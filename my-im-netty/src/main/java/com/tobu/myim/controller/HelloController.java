package com.tobu.myim.controller;


import com.tobu.myim.netty.NettyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private NettyConfig nettyConfig;

    @Value("${image.face.upload.path}")
    private String path;

    @GetMapping("/hello")
    public String test(){
        return "success"+path;
    }

}
