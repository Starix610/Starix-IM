package com.tobu.myim.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class WSServerInitialzer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                //websocket基于http协议，所以要有http编解码器
                .addLast("HttpServerCodec",new HttpServerCodec())
                //对于写大数据流的支持
                .addLast(new ChunkedWriteHandler())
                //对httpMessage进行聚合，聚合成FullHttpRequest或FullHttpResponse
                //在netty的编程中几乎都会用到这个handler
                .addLast(new HttpObjectAggregator(1024*64))




                // ====================== 增加心跳支持 start  ======================
                // 针对客户端，如果在1分钟时没有向服务端发送读写心跳(ALL)，则主动断开
                // 如果是读空闲或者写空闲，不处理
                .addLast(new IdleStateHandler(60, 60, 60))
                // 自定义的空闲状态检测
                .addLast(new HeartBeatHandler())
                 // ====================== 增加心跳支持 end    ======================


                /**
                 * websocket服务器处理的协议，并指定客户端访问的路由
                 * 本handler会帮你处理一些繁重复杂的事情
                 * 会帮你处理握手动作（close，ping，pong）
                 * websocket数据是以frames进行传输的
                 */
                .addLast(new WebSocketServerProtocolHandler("/ws"))
                // 自定义的handler
                .addLast("ChatHandler",new ChatHandler());
    }
}
