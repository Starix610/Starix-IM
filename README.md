# My-IM
基于netty支持的websocket的移动端即时聊天项目V1.0.0


1、本项目基于慕课网实战课程《Java仿微信全栈 高性能后台+移动客户端》
虽然说仿微信，但其实和微信差远了。此项目仅作为netty的一个实战练习项目。

2、前端是使用MUI构建的移动App客户端，后端主要使用Springboot+Netty。

3、在原课程基础上更改或者优化了一些地方：
（1）图片上传没有采用FastDFS文件服务器，而是直接将图片保存到服务器本地磁盘，缩略图使用thumbnailator开源项目生成，用Nginx作为静态资源访问的http服务器。
（2）优化了客户端的一些UI问题，比如图片显示、十分bug的聊天气泡显示问题等。
（3）后端代码的优化，比如Springboot和netty的整合的代码，部分业务逻辑代码等。
（4）客户端扫码的体验问题(暂时还存在一点问题)。
（5）优化了客户端登录与登出的页面处理。

