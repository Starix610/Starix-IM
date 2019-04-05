package com.tobu.myim.service.impl;

import com.tobu.myim.enums.MsgActionEnum;
import com.tobu.myim.enums.MsgSignFlagEnum;
import com.tobu.myim.enums.SearchFriendsStatusEnum;
import com.tobu.myim.mapper.ChatMsgMapper;
import com.tobu.myim.mapper.FriendsRequestMapper;
import com.tobu.myim.mapper.MyFriendsMapper;
import com.tobu.myim.mapper.UsersMapper;
import com.tobu.myim.netty.ChatMsg;
import com.tobu.myim.netty.DataContent;
import com.tobu.myim.netty.UserChannelRel;
import com.tobu.myim.pojo.FriendsRequest;
import com.tobu.myim.pojo.MyFriends;
import com.tobu.myim.pojo.Users;
import com.tobu.myim.pojo.vo.FriendRequestVO;
import com.tobu.myim.pojo.vo.MyFriendsVO;
import com.tobu.myim.service.UserService;
import com.tobu.myim.utils.JSONResult;
import com.tobu.myim.utils.JsonUtils;
import com.tobu.myim.utils.MD5Utils;
import com.tobu.myim.utils.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private Sid sid;
    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Value("${image.qrcode.upload.path}")
    private String qrcodeUploadPath;
    @Value("${image.qrcode.url.prefix}")
    private String qrcodeUrlPrefix;

    @Autowired
    private MyFriendsMapper myFriendsMapper;
    @Autowired
    private FriendsRequestMapper friendsRequestMapper;
    @Autowired
    private ChatMsgMapper chatMsgMapper;


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryForUsernameIsExist(String username) {
        Users user = new Users();
        user.setUsername(username);
        Users result = usersMapper.selectOne(user);

        return result==null?false:true;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String password) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", password);
        Users result = usersMapper.selectOneByExample(userExample);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) throws Exception {
        String userId = sid.nextShort();
        user.setId(userId);
        user.setNickname(user.getUsername());
        user.setFaceImage("");
        user.setFaceImageBig("");
        user.setPassword(MD5Utils.getMD5Str(user.getPassword()));

        //生成用户二维码
        String qrcodeImgName = userId + "qrcode.png";
        String qrcodeImgPath = qrcodeUploadPath + qrcodeImgName;
        qrCodeUtils.createQRCode(qrcodeImgPath, "my_im_qrcode:"+user.getUsername());
        String qrcodeImgUrl = qrcodeUrlPrefix + qrcodeImgName;
        user.setQrcode(qrcodeImgUrl);

        usersMapper.insert(user);

        return user;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users user) {
        usersMapper.updateByPrimaryKeySelective(user);
        Users userResult = queryUserById(user.getId());
        return userResult;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserById(String userId) {
        Users user = usersMapper.selectByPrimaryKey(userId);
        return user;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfoByUsername(String friendUsername) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", friendUsername);
        Users user = usersMapper.selectOneByExample(userExample);
        return user;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Integer preconditionAddFriends(String myUserId, String friendUsername) {

        Users user = queryUserInfoByUsername(friendUsername);

        // 1. 添加的用户如果不存在，返回[无此用户]
        if (user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }

        // 2. 添加的账号是你自己的账号，返回[不能添加自己]
        if (user.getId().equals(myUserId)){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }

        // 3. 添加的朋友已经是你的好友，返回[该用户已经是你的好友]
        Example myFriendsExample = new Example(MyFriends.class);
        Example.Criteria criteria = myFriendsExample.createCriteria();
        criteria.andEqualTo("myUserId", myUserId);
        criteria.andEqualTo("myFriendUserId", user.getId());
        MyFriends myFriendRelationship = myFriendsMapper.selectOneByExample(myFriendsExample);
        if (myFriendRelationship!=null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }

        return SearchFriendsStatusEnum.SUCCESS.status;

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public JSONResult sendFriendRequest(String myUserId, String friendUsername) {


        // 前置条件 - 1. 添加的用户如果不存在，返回[无此用户] (假如越过前端发送请求)
        // 前置条件 - 2. 添加的账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 添加的朋友已经是你的好友，返回[该用户已经是你的好 友]

        Integer status = preconditionAddFriends(myUserId, friendUsername);

        //如果满足了添加好友条件
        if (status == SearchFriendsStatusEnum.SUCCESS.status){
            Users friend  = queryUserInfoByUsername(friendUsername);
            // 1.查询发送好友请求记录表
            Example fre = new Example(FriendsRequest.class);
            Example.Criteria frc = fre.createCriteria();
            frc.andEqualTo("sendUserId", myUserId);
            frc.andEqualTo("acceptUserId", friend.getId());
            FriendsRequest friendRequest = friendsRequestMapper.selectOneByExample(fre);
            if ( friendRequest ==null ){
                // 2. 如果好友添加请求表没有记录，则新增好友请求记录
                String requestId = sid.nextShort();
                FriendsRequest request = new FriendsRequest();
                request.setId(requestId);
                request.setSendUserId(myUserId);
                request.setAcceptUserId(friend.getId());
                request.setRequestDateTime(new Date());
                friendsRequestMapper.insert(request);
            }
            return JSONResult.ok();
        }else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(errorMsg);
        }
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return usersMapper.queryFriendRequestList(acceptUserId);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        Example fre = new Example(FriendsRequest.class);
        Example.Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", sendUserId);
        frc.andEqualTo("acceptUserId", acceptUserId);
        friendsRequestMapper.deleteByExample(fre);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        saveFriends(sendUserId,acceptUserId);
        saveFriends(acceptUserId,sendUserId);
        deleteFriendRequest(sendUserId,acceptUserId);

        // 使用websocket主动推送消息到请求发起者，更新他的通讯录列表为最新
        Channel senderChannel = UserChannelRel.get(sendUserId);
        if (senderChannel != null) {
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
            senderChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveFriends(String sendUserId, String acceptUserId){

        MyFriends myFriends = new MyFriends();
        myFriends.setId(sid.nextShort());
        myFriends.setMyUserId(acceptUserId);
        myFriends.setMyFriendUserId(sendUserId);
        myFriendsMapper.insert(myFriends);

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {

        List<MyFriendsVO> list = myFriendsMapper.queryMyFriends(userId);
        return list;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.tobu.myim.pojo.ChatMsg msgDB = new com.tobu.myim.pojo.ChatMsg();
        String msgId = sid.nextShort();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msgDB);
        return msgId;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateMsgSigned(String[] msgIds) {
        chatMsgMapper.batchUpadteSigned(msgIds);
    }

    @Override
    public List<com.tobu.myim.pojo.ChatMsg> queryUnReadMsgList(String userId) {
        Example example = new Example(com.tobu.myim.pojo.ChatMsg.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("signFlag", 0);
        criteria.andEqualTo("acceptUserId", userId);
        List<com.tobu.myim.pojo.ChatMsg> unReadMsgList = chatMsgMapper.selectByExample(example);
        return unReadMsgList;
    }
}

