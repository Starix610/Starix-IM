package com.tobu.myim;

import com.tobu.myim.netty.NettyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
// 扫描mybatis mapper包路径
@MapperScan(basePackages="com.tobu.myim.mapper")
// 扫描 所有需要的包, 包含一些自用的工具类包 所在的路径
@ComponentScan(basePackages= {"com.tobu","org.n3r.idworker"})
// 属性绑定配置文件
@EnableConfigurationProperties({NettyConfig.class})
public class MyImApplication {


    public static void main(String[] args) {
        SpringApplication.run(MyImApplication.class, args);
    }

}
