package com.xuecheng.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.IOrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/19 20:26
 * @Description 订单支付接口
 */
@Api(value = "订单支付接口", tags = "订单支付接口")
@Slf4j
@RequiredArgsConstructor
@Controller
public class OrderController {

    private final IOrderService orderService;

    @Value("${pay.alipay.APP_ID}")
    private String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    private String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    private String ALIPAY_PUBLIC_KEY;

    @Value("${pay.internalUrl}")
    private String internalNetworkUrl;


    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("请登录后继续选课");
        }
        return orderService.createOrder(user.getId(), addOrderDto);
    }

    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestPay(String payNo, HttpServletResponse httpResponse) throws IOException, AlipayApiException {
        // 校验payNo支付记录交易号是否存在
        XcPayRecord payRecord = orderService.getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            XueChengPlusException.cast("支付记录交易号不存在");
        }
        // 查看支付结果
        String status = payRecord.getStatus();
        if ("601002".equals(status)) {
            XueChengPlusException.cast("已支付，无需重复支付");
        }

        //获得初始化的AlipayClient
        AlipayClient client = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT,
                AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        //创建API对应的request
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        // alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        // 告诉支付宝支付结果通知的地址，在公共参数中设置回跳和通知地址
        alipayRequest.setNotifyUrl(internalNetworkUrl + "/orders/receivenotify");
        // 填充业务参数
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"" + payRecord.getPayNo() + "\"," +// 商户网站唯一订单号，本项目指定支付记录的交易号
                "    \"total_amount\":" + payRecord.getTotalPrice() + "," +
                "    \"subject\":\"" + payRecord.getOrderName() + "\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }");
        String form = client.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
    }

    // 接收支付宝支付结果通知
    @RequestMapping("/receivenotify")
    public void receiveNotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, IOException {
        // 获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        // 验签名
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if (verify_result) {//验证成功
            // 商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
            // 支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
            // 交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
            // 支付宝通过我们的appid
            String appid = new String(request.getParameter("app_id").getBytes("ISO-8859-1"), "UTF-8");
            // 总金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");

            if (trade_status.equals("TRADE_SUCCESS")) {
                System.out.println("================支付成功==================");
                // 更新订单表的状态
                // 封装一个
                PayStatusDto payStatusDto = new PayStatusDto();
                // 支付宝通过我们的appid
                payStatusDto.setApp_id(appid);
                // 支付结果
                payStatusDto.setTrade_status(trade_status);
                // 商户自己的订单号
                payStatusDto.setOut_trade_no(out_trade_no);
                // 总金额
                payStatusDto.setTotal_amount(total_amount);
                // 支付宝自己的订单
                payStatusDto.setTrade_no(trade_no);
                // 保存支付宝交易状态
                orderService.saveAliPayStatus(payStatusDto);
            }
            // ——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
            response.getWriter().println("success");
        } else {//验证失败
            response.getWriter().println("fail");
        }
    }

    @ApiOperation("查询支付结果")
    @GetMapping("/payresult")
    @ResponseBody
    public PayRecordDto payresult(String payNo) throws IOException {
        log.debug("payNo:{}", payNo);
        return orderService.queryPayResult(payNo);
    }
}
