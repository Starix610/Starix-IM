package com.tobu.myim.mapper;

import com.tobu.myim.pojo.MyFriends;
import com.tobu.myim.pojo.vo.MyFriendsVO;
import com.tobu.myim.utils.MyMapper;

import java.util.List;

public interface MyFriendsMapper extends MyMapper<MyFriends> {

    List<MyFriendsVO> queryMyFriends(String userId);

}