package com.xuecheng.content.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lniiwuw
 */
@Slf4j
@RestController
@RequestMapping("mqMessage")
public class MqMessageController {

    @Autowired
    private MqMessageService  mqMessageService;
}
