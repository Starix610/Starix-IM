package com.tobu.myim.mapper;

import com.tobu.myim.pojo.ChatMsg;
import com.tobu.myim.utils.MyMapper;

public interface ChatMsgMapper extends MyMapper<ChatMsg> {

    void batchUpadteSigned(String[] msgIds);

}