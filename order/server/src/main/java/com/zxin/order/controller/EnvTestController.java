package com.zxin.order.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/env")
@RefreshScope
public class EnvTestController {

    @Value("${env}")
    public String env;

    @GetMapping("/print")
    public String print(){
        return env;
    }

}
