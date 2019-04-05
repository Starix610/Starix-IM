package com.tobu.myim.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceWebMvcConfig implements WebMvcConfigurer {

    @Value("${image.face.upload.path}")
    private String faceUploadPath;
    @Value("${image.qrcode.upload.path}")
    private String qrcodeUploadPath;
    @Value("${image.face.url.prefix}")
    private String faceUrlPrefix;
    @Value("${image.qrcode.url.prefix}")
    private String qrcodeUrlPrefix;

    
    /**
    * 本地测试时使用的配置，用于上传的图片资源的访问
    */
    
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/images/"+faceUrlPrefix+"**").addResourceLocations("file:"+faceUploadPath);
//        registry.addResourceHandler("/images/"+qrcodeUrlPrefix+"**").addResourceLocations("file:"+qrcodeUploadPath);
//    }

}
