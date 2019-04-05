package com.tobu.myim.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WSServer {

    private static final Logger logger = LoggerFactory.getLogger(WSServer.class);

    @Autowired
    private NettyConfig nettyConfig;

    // 单例模式获得WSServer实例
//    private static class SingletionWSServer{
//        //类一加载就实例化WSServer，并且是单例模式（静态内部类形式），保证只有一个容器中只有一个实例
//        static final WSServer instance = new WSServer();
//    }
//    public static WSServer getInstance(){
//        return SingletionWSServer.instance;
//    }

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;
    private ChannelFuture future;

    public WSServer(){
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup);
        bootstrap.channel(NioServerSocketChannel.class).childHandler(new WSServerInitialzer());
    }

    public void start(){
        try {
            this.future = bootstrap.bind(nettyConfig.getPort()).sync();
            logger.info("Netty Server ["+WSServer.class.getName()+"] started and listening on" + future.channel().localAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
