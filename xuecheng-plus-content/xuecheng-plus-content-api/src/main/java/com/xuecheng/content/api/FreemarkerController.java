package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/11 21:01
 * @Description Freemarker渲染前端界面测试
 */
@Controller
public class FreemarkerController {

    @GetMapping("/testfreemaker")
    public ModelAndView test() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("test");
        modelAndView.addObject("broski", "Kyle");
        return modelAndView;
    }
}
