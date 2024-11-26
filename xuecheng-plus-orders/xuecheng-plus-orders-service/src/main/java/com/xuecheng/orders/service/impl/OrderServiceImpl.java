package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/19 20:28
 * @Description 订单接口实现类
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements IOrderService {

    private final XcOrdersMapper xcOrdersMapper;
    private final XcOrdersGoodsMapper xcOrdersGoodsMapper;
    private final XcPayRecordMapper xcPayRecordMapper;
    private final MqMessageService mqMessageService;
    private final RabbitTemplate rabbitTemplate;

    @Lazy
    @Autowired
    IOrderService currentProxy;

    @Value("${pay.alipay.APP_ID}")
    private String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    private String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    private String ALIPAY_PUBLIC_KEY;

    @Value("${pay.internalUrl}")
    private String internalNetworkUrl;

    // nacos配置的支付二维码url
    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        // 1. 添加商品订单
        XcOrders xcOrders = saveOrders(userId, addOrderDto);

        // 2. 添加支付交易记录
        XcPayRecord payRecord = createPayRecord(xcOrders);

        // 3. 生成二维码
        String qrCode = null;
        try {
            //url 要可以被模拟器访问到，url 为下单接口(稍后定义)
            String url = String.format(qrcodeurl, payRecord.getPayNo());
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        } catch (IOException e) {
            log.debug("生成二维码出错");
            XueChengPlusException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayNo(String payNo) {
        return xcPayRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {
        XcPayRecord payRecord = getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            XueChengPlusException.cast("请重新点击支付获取二维码");
        }
        //支付状态
        String status = payRecord.getStatus();
        //如果支付成功直接返回
        if ("601002".equals(status)) {
            PayRecordDto payRecordDto = new PayRecordDto();
            BeanUtils.copyProperties(payRecord, payRecordDto);
            return payRecordDto;
        }
        //从支付宝查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //保存支付结果
        currentProxy.saveAliPayStatus(payStatusDto);
        //查询最新的支付记录
        payRecord = getPayRecordByPayNo(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        return payRecordDto;
    }

    @Transactional
    @Override
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        // 支付宝交易状态
        String tradeStatus = payStatusDto.getTrade_status();
        // 判断是否支付成功，若没有支付成功，无需进行后续操作
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            return;
        }

        // 1. 获取支付流水号
        String payNo = payStatusDto.getOut_trade_no();
        // 2. 查询数据库订单状态
        XcPayRecord payRecord = getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            log.info("收到支付结果通知查询不到支付记录，收到的信息：{}", payStatusDto);
            XueChengPlusException.cast("未找到支付记录");
        }
        // 2.1 已支付，直接返回
        String statusFromDB = payRecord.getStatus();
        // 从【数据库】查询到的支付状态已经成功，则不再处理
        if ("601002".equals(statusFromDB)) {
            log.info("收到支付结果通知，支付记录状态已经为支付成功，不进行任务操作");
            return;
        }
        // 查询相应订单
        Long orderId = payRecord.getOrderId();
        XcOrders order = xcOrdersMapper.selectById(orderId);
        if (order == null) {
            log.info("收到支付宝支付结果通知，查询不到订单,支付宝传过来的参数：{}，订单号：{}", payStatusDto, orderId);
            XueChengPlusException.cast("找不到相关联的订单");
        }

        // 3. 更新交易状态
        // 3.1 更新订单记录表
        // 更新支付记录表的状态为支付成功
        payRecord.setStatus("601002");
        // 第三方支付交易流水号
        payRecord.setOutPayNo(payStatusDto.getTrade_no());
        // 第三方支付渠道
        payRecord.setOutPayChannel("Alipay");
        // 通过支付宝支付的状态码
        payRecord.setOutPayChannel("603002");
        payRecord.setPaySuccessTime(LocalDateTime.now());
        int updateRecord = xcPayRecordMapper.updateById(payRecord);
        if (updateRecord <= 0) {
            log.info("收到支付宝支付结果通知，更新支付记录表失败：{}", payStatusDto);
            XueChengPlusException.cast("更新支付交易表失败");
        }
        log.info("收到支付宝支付结果通知，更新支付记录表成功：{}", payStatusDto);

        // 3.2 更新订单表
        // 更新订单状态为支付成功
        order.setStatus("600002");
        int updateOrder = xcOrdersMapper.updateById(order);
        if (updateOrder <= 0) {
            log.info("收到支付宝支付结果通知，更新订单表失败,支付宝传过来的参数：{}，订单号：{}", payStatusDto, orderId);
            XueChengPlusException.cast("更新订单表失败");
        }
        log.info("收到支付宝支付结果通知，更新订单表成功,支付宝传过来的参数：{}，订单号：{}", payStatusDto, orderId);

        // 4. 保存消息记录，参数1：支付结果类型通知；参数2：业务id；参数3：业务类型
        MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", order.getOutBusinessId(), order.getOrderType(), null);
        // 5. 向mq通知消息
        notifyPayResult(mqMessage);
    }

    @Override
    public void notifyPayResult(MqMessage mqMessage) {
        // 1. 将消息体转为Json
        String jsonMsg = JSON.toJSONString(mqMessage);
        // 2. 设消息的持久化方式为PERSISTENT，即消息会被持久化到磁盘上，确保即使在RabbitMQ服务器重启后也能够恢复消息。
        Message msgObj = MessageBuilder.withBody(jsonMsg.getBytes()).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
        // 3. 封装CorrelationData，用于跟踪消息的相关信息
        CorrelationData correlationData = new CorrelationData(mqMessage.getId().toString());
        // 3.1 ConfirmCallBack机制：添加一个Callback对象，该对象用于在消息确认时处理消息的结果
        correlationData.getFuture().addCallback(result -> {
            assert result != null;
            if (result.isAck()) {
                // 3.2 消息发送成功，删除消息表中的记录
                log.debug("消息发送成功：{}", jsonMsg);
                // 删除mqMessage消息
                mqMessageService.completed(mqMessage.getId());
            } else {
                // 3.3 消息发送失败
                log.error("消息发送失败，id：{}，原因：{}", mqMessage.getId(), result.getReason());
            }
        }, ex -> {
            // 3.4 消息异常
            log.error("消息发送异常，id：{}，原因：{}", mqMessage.getId(), ex.getMessage());
        });
        // 4. 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj, correlationData);
    }

    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 系统支付交易号
     * @return PayStatusDto
     */
    private PayStatusDto queryPayResultFromAlipay(String payNo) {
        //========请求支付宝查询支付结果=============
        // 获得初始化的 AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT,
                    AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                XueChengPlusException.cast("请求支付查询查询失败");
            }
        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}", e.toString(), e);
            XueChengPlusException.cast("请求支付查询查询失败");
        }
        // 获取支付结果
        String resultJson = response.getBody();
        // 4.1 转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map<String, String> alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        // 交易状态
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        // 交易金额
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        // 支付宝交易号
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        //保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);
        return payStatusDto;
    }


    /**
     * 根据订单表生成支付交易记录
     *
     * @param orders 订单表信息
     * @return XcPayRecord
     */
    public XcPayRecord createPayRecord(XcOrders orders) {
        if(orders == null){
            XueChengPlusException.cast("订单不存在");
        }

        if(orders.getStatus().equals("600002")){
            XueChengPlusException.cast("订单已支付");
        }

        XcPayRecord payRecord = new XcPayRecord();
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo); // 支付记录交易号
        // 记录关键订单id
        payRecord.setOrderId(orders.getId());
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001"); // 未支付
        payRecord.setUserId(orders.getUserId());
        // 插入支付交易表
        int insert = xcPayRecordMapper.insert(payRecord);
        if (insert <= 0) {
            XueChengPlusException.cast("插入支付交易记录失败");
        }
        return payRecord;
    }

    /**
     * 保存订单信息，保存订单表和订单明细表，需要做幂等性判断
     *
     * @param userId      用户id
     * @param addOrderDto 选课信息
     * @return XcOrders
     */
    public XcOrders saveOrders(String userId, AddOrderDto addOrderDto) {
        // 1. 幂等性判断 是否存在对应的课程订单
        XcOrders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (order != null) {
            return order;
        }
        // 2. 插入订单表
        order = new XcOrders();
        BeanUtils.copyProperties(addOrderDto, order);
        // 雪花算法生成id
        order.setId(IdWorkerUtils.getInstance().nextId());
        order.setCreateDate(LocalDateTime.now());
        order.setUserId(userId);
        order.setStatus("600001"); // 未支付
        int insert = xcOrdersMapper.insert(order);
        if (insert <= 0) {
            XueChengPlusException.cast("插入订单记录失败");
        }
        // 3. 插入订单明细表
        Long orderId = order.getId();
        String orderDetail = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetail, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods -> {
            goods.setOrderId(orderId);
            int insert1 = xcOrdersGoodsMapper.insert(goods);
            if (insert1 <= 0) {
                XueChengPlusException.cast("插入订单明细失败");
            }
        });
        return order;
    }

    /**
     * 根据业务id（课程id）查询相应订单
     *
     * @param outBusinessId  外部系统业务id（课程id）
     * @return XcOrders
     */
    private XcOrders getOrderByBusinessId(String outBusinessId) {
        LambdaQueryWrapper<XcOrders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcOrders::getOutBusinessId, outBusinessId);
        return xcOrdersMapper.selectOne(queryWrapper);
    }
}
