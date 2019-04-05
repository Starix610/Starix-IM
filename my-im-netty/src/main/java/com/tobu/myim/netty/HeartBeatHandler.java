package com.tobu.myim.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Description: 用于检测channel的心跳handler
 * 				 继承ChannelInboundHandlerAdapter，从而不需要实现channelRead0方法
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 判断evt是否是IdleStateEvent（用于触发用户事件，包括读空闲/写空闲/读写空闲，当空闲时间超过设定值后还未收到心跳信息则执行相关的操作）
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;		// 强制类型转换

            if (event.state() == IdleState.READER_IDLE){
//                System.out.println("channel["+ctx.channel().id().asShortText()+"]进入读空闲...");
            } else if (event.state() == IdleState.WRITER_IDLE) {
//                System.out.println("channel["+ctx.channel().id().asShortText()+"]进入写空闲...");
            } else if (event.state() == IdleState.ALL_IDLE) {
                System.out.println("channel关闭前，userChannels：" + ChatHandler.userChannels.size());

                Channel channel = ctx.channel();
                // 关闭超时未发送心跳包的无用的channel，以防资源浪费
                channel.close();

//                System.out.println("channel关闭后，userChannels：" + ChatHandler.userChannels.size());
            }
        }

    }
}
