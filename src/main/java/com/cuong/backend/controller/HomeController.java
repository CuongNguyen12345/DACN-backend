package com.cuong.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class HomeController {
    
    @GetMapping
    public String getMethodName() {
        return "success";
    }
    
}
