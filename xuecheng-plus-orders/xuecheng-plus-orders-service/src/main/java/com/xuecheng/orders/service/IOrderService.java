package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcPayRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/19 20:27
 * @Description 订单相关接口
 */
public interface IOrderService {
    /**
     * 创建商品订单
     *
     * @param userId        用户id
     * @param addOrderDto   订单信息
     * @return  支付交易记录
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * 保存支付宝支付结果
     *
     * @param payStatusDto 从支付宝查询的支付结果信息
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 根据系统支付交易号查询支付订单信息
     *
     * @param payNo 系统支付交易号
     * @return XcPayRecord
     */
    XcPayRecord getPayRecordByPayNo(String payNo);

    /**
     * 请求支付宝查询支付结果，并更新数据库交易订单和流水状态
     *
     * @param payNo 支付记录 id
     * @return 支付记录信息
     */
    public PayRecordDto queryPayResult(String payNo);

    /**
     * 向mq发送通知结果
     * @param mqMessage 消息
     */
    void notifyPayResult(MqMessage mqMessage);
}
