package com.tobu.myim.service;

import com.tobu.myim.netty.ChatMsg;
import com.tobu.myim.pojo.Users;
import com.tobu.myim.pojo.vo.FriendRequestVO;
import com.tobu.myim.pojo.vo.MyFriendsVO;
import com.tobu.myim.utils.JSONResult;

import java.util.List;

public interface UserService {

    /**
     * @Description: 判断用户名是否存在
     */
    boolean queryForUsernameIsExist(String username);

    /**
     * @Description: 查询用户是否存在
     */
    Users queryUserForLogin(String username, String password);

    /**
     * @Description: 用户注册
     */
    Users saveUser(Users user) throws Exception;

    /**
     * @Description: 修改用户信息
     */
    Users updateUserInfo(Users user);

    /**
     * @Description: 添加好友的前置条件
     */
    Integer preconditionAddFriends(String myUserId, String friendUsername);

    /**
     * @Description: 根据用户名查询用户对象
     */
    Users queryUserInfoByUsername(String friendUsername);

    /**
     * @Description: 添加好友请求记录，保存到数据库
     */
    JSONResult sendFriendRequest(String myUserId, String friendUsername);

    /**
     * @Description: 查询好友请求列表
     */
    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    /**
     * @Description: 删除好友请求记录
     */
    void deleteFriendRequest(String sendUserId, String acceptUserId);


    /**
     * @Description: 通过好友请求
     * 				1. 保存好友
     * 				2. 逆向保存好友
     * 				3. 删除好友请求记录
     */
    void passFriendRequest(String sendUserId, String acceptUserId);


    /**
     * @Description: 查询我的好友列表
     */
    List<MyFriendsVO> queryMyFriends(String userId);

    /**
     * @Description: 保存聊天消息到数据库
     */
    String saveMsg(ChatMsg chatMsg);

    /**
     * @Description: 批量签收消息
     */
    void updateMsgSigned(String[] msgIds);

    /**
     * @Description: 获取未签收消息列表
     */
    List<com.tobu.myim.pojo.ChatMsg> queryUnReadMsgList(String userId);
}
