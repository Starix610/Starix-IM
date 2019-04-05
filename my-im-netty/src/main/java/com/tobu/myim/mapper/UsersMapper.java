package com.tobu.myim.mapper;

import com.tobu.myim.pojo.Users;
import com.tobu.myim.pojo.vo.FriendRequestVO;
import com.tobu.myim.utils.MyMapper;

import java.util.List;

public interface UsersMapper extends MyMapper<Users> {

    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

}