package com.tobu.myim.netty;

import com.tobu.myim.enums.MsgActionEnum;
import com.tobu.myim.service.UserService;
import com.tobu.myim.utils.JsonUtils;
import com.tobu.myim.utils.SpringContextUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * TextWebSocketFrame : 在websokcet中专门用于处理文本数据的对象
 */

public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    //ChannelGroup用于记录和管理所有客户端的channel，保存到一个组里面进行管理
    public static ChannelGroup userChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        // 获取客户端传输过来的消息(Json格式)
        String content = msg.text();

        Channel currentChannel = ctx.channel();

        // 1. 解析客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();

        // 2. 判断消息类型，根据不同的类型来处理不同的业务
        if (action == MsgActionEnum.CONNECT.type){
            // 	2.1  当websocket 第一次open的时候，初始化channel，把用的channel和userid关联起来
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId, currentChannel);
//            System.out.println("当前连接的客户端信息：");
//            UserChannelRel.output();
        }else if (action == MsgActionEnum.CHAT.type) {
            //  2.2  聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态[未签收]
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();
            String senderId = chatMsg.getSenderId();

            //保存消息到数据库,并标记未签收
            UserService userService = (UserService) SpringContextUtil.getBean("userServiceImpl");
            String msgId =  userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);
            
            //发送消息给接收者
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if (receiverChannel == null){
                // TODO: 2019/4/2 为空代表用户离线，进行推送消息（JPush，个推，小米推送）
            }else {
                Channel findChannel = userChannels.find(receiverChannel.id());
                if (findChannel!=null){
                    //用户在线
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContentMsg)));
                }else {
                    //用户依旧离线
                    // TODO: 2019/4/2 进行消息推送
                }
            }

        }else if (action == MsgActionEnum.SIGNED.type) {
            //  2.3  签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态[已签收]

            // 扩展字段在signed类型的消息中，代表需要去签收的消息(多个)id，逗号间隔
            String msgIdsStr = dataContent.getExtand();
            String[] msgIds = msgIdsStr.split(",");
            if (msgIds!=null && msgIds.length>0){
                UserService userService = (UserService) SpringContextUtil.getBean("userServiceImpl");
                //批量签收
                userService.updateMsgSigned(msgIds);
            }

        }else if (action == MsgActionEnum.KEEPALIVE.type) {
            //  2.4  心跳类型的消息
//            System.out.println("收到来自channel为[" + currentChannel + "]的心跳包...");
        }

    }


    /**
     *  当客户端连接服务端后，获取客户端当前客户端对应的handler,添加到group中
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        userChannels.add(ctx.channel());
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //当触发handlerRemoved时会自动移除对应客户端的channel,可以不写下面这行代码
        userChannels.remove(ctx.channel());
//        System.out.println("客户端断开连接，channel长id为："+ctx.channel().id().asLongText());
//        System.out.println("客户端断开连接，channel短id为："+ctx.channel().id().asShortText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常之后关闭连接（关闭channel），随后从ChannelGroup中移除
        userChannels.remove(ctx.channel());
    }
}
