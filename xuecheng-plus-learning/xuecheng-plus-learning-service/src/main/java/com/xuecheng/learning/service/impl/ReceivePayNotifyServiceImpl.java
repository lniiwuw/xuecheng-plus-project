package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.IMyCourseTablesService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/21 21:00
 * @Description 接收mq消息通知
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReceivePayNotifyServiceImpl {

    private final IMyCourseTablesService tablesService;

    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message) {
        // 1. 获取消息
        MqMessage mqMessage = JSON.parseObject(message.getBody(), MqMessage.class);
        // 2. 根据我们存入的消息，进行解析
        // 2.1 消息类型，学习中心只处理支付结果的通知
        String messageType = mqMessage.getMessageType();
        // 2.2 外部系统业务id（选课记录id）
        String chooseCourseId = mqMessage.getBusinessKey1();
        // 2.3 订单类型，60201表示购买课程，学习中心只负责处理这类订单请求
        String orderType = mqMessage.getBusinessKey2();
        // 3. 学习中心只负责处理支付结果的通知学习中心和购买课程类订单的结果
        if (PayNotifyConfig.MESSAGE_TYPE.equals(messageType) && "60201".equals(orderType)){
            log.debug("拿到课程支付信息，选课记录id：{}", chooseCourseId);
            // 3.1 保存选课记录
            boolean flag = tablesService.saveChooseCourseStatus(chooseCourseId);
            if (!flag) {
                XueChengPlusException.cast("保存选课记录失败");
            }
        }
    }
}
