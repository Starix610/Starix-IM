package com.tobu.myim.controller;


import com.tobu.myim.enums.OperatorFriendRequestTypeEnum;
import com.tobu.myim.pojo.ChatMsg;
import com.tobu.myim.pojo.Users;
import com.tobu.myim.pojo.bo.UsersBO;
import com.tobu.myim.pojo.vo.FriendRequestVO;
import com.tobu.myim.pojo.vo.MyFriendsVO;
import com.tobu.myim.pojo.vo.UsersVO;
import com.tobu.myim.service.UserService;
import com.tobu.myim.utils.FileUtils;
import com.tobu.myim.utils.JSONResult;
import com.tobu.myim.utils.MD5Utils;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/u")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${image.face.upload.path}")
    private String uploadPath;

    @Value("${image.face.url.prefix}")
    private String faceUrlPrefix;

    /**
     * 注册或登录
     * @param user
     * @return
     * @throws Exception
     */
    @PostMapping("/registerOrLogin")
    public JSONResult registerOrLogin(@RequestBody Users user) throws Exception {

        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())){
            return JSONResult.errorMsg("用户名或密码不能为空!");
        }
        boolean usernameIsExist = userService.queryForUsernameIsExist(user.getUsername());

        Users userResult = null;
        //如果用户名存在则登录，不存在则注册
        if (usernameIsExist){
            userResult = userService.queryUserForLogin(user.getUsername(), MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null) {
                return JSONResult.errorMsg("用户名或密码错误!");
            }
        }else {
            userResult = userService.saveUser(user);
        }
        UsersVO userVo = new UsersVO();
        BeanUtils.copyProperties(userResult, userVo);

        return JSONResult.ok(userVo);
    }

    /**
     * 头像图片上传保存
     * @param userBO
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadFaceBase64")
    public JSONResult uploadFaceBase64(@RequestBody UsersBO userBO) throws Exception {

        //获取前端传过来的base64字符串，然后转换为文件存储到服务器上
        String base6Data = userBO.getFaceData();

        String randomName = UUID.randomUUID().toString().replaceAll("-", "");
        String faceImgName = randomName+".jpg";
        String thumbnailFaceImgName = randomName +"_80×80.png";

        String faceImgUploadPath = uploadPath + faceImgName;
        String thumbnailFaceImgUploadPath = uploadPath + thumbnailFaceImgName;

        FileUtils.base64ToFile(faceImgUploadPath, base6Data);
        Thumbnails.of(faceImgUploadPath).size(80, 80).toFile(thumbnailFaceImgUploadPath); //压缩图片大小至80×80并另存一份

        String userFaceUrl = faceUrlPrefix + faceImgName;
        String userFaceUrlThumbnail = faceUrlPrefix + thumbnailFaceImgName;

        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setFaceImageBig(userFaceUrl);
        user.setFaceImage(userFaceUrlThumbnail);

        Users userResult = userService.updateUserInfo(user);
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(userResult, userVO);
        return JSONResult.ok(userVO);
    }

    /**
     * 设置昵称
     * @param userBO
     * @return
     * @throws Exception
     */
    @PostMapping("/setNickname")
    public JSONResult setNickname(@RequestBody UsersBO userBO) throws Exception {

        if (userBO.getNickname().length()<1){
            return JSONResult.errorMsg("昵称长度太短!");
        }else if(userBO.getNickname().length()>8){
            return JSONResult.errorMsg("昵称长度不能超过8位!");
        }

        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());

        Users userResult = userService.updateUserInfo(user);
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(userResult, userVO);

        return JSONResult.ok(userVO);
    }

    /**
     * 搜索好友接口
     */
    @PostMapping("/search")
    public JSONResult search(String myUserId, String friendUsername) throws Exception {

        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return JSONResult.errorMsg("缺少参数");
        }

        // 搜索的用户如果不存在，返回[不存在此用户]
        Users user = userService.queryUserInfoByUsername(friendUsername);
        if (user == null){
            return JSONResult.errorMsg("用户不存在");
        }else {
            UsersVO userVO = new UsersVO();
            BeanUtils.copyProperties(user, userVO);
            return JSONResult.ok(userVO);
        }
    }

    /**
     * 添加好友接口
     */
    @PostMapping("/addFriendRequest")
    public JSONResult addFriendRequest(String myUserId, String friendUsername) throws Exception {

        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return JSONResult.errorMsg("缺少参数");
        }

        JSONResult result = userService.sendFriendRequest(myUserId,friendUsername);

        return result;

    }

    /**
     * 查询好友请求列表
     */
    @PostMapping("/queryFriendRequests")
    public JSONResult queryFriendRequests(String userId) throws Exception {

        if (StringUtils.isBlank(userId)){
            return JSONResult.errorMsg("缺少参数");
        }

        List<FriendRequestVO> requestList = userService.queryFriendRequestList(userId);

        return JSONResult.ok(requestList);

    }


    /**
     * 通过或忽略好友请求
     */
    @PostMapping("/operFriendRequest")
    public JSONResult operFriendRequest(String acceptUserId,String sendUserId,Integer operType) throws Exception {

        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId) || operType == null) {
            return JSONResult.errorMsg("缺少参数");
        }

        // 1. 如果operType 没有对应的枚举值，则直接抛出空错误信息
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return JSONResult.errorMsg("缺少参数");
        }

        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type) {
            // 2. 判断如果忽略好友请求，则直接删除好友请求的数据库表记录
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        } else if (operType == OperatorFriendRequestTypeEnum.PASS.type) {
            // 3. 判断如果是通过好友请求，则互相增加好友记录到数据库对应的表
            //    然后删除好友请求的数据库表记录
            userService.passFriendRequest(sendUserId, acceptUserId);
        }

        // 4. 数据库查询好友列表，返回给前端新的用户列表
        List<MyFriendsVO> myFirends = userService.queryMyFriends(acceptUserId);

        return JSONResult.ok(myFirends);

    }


    /**
     * 查询我的好友列表
     */
    @PostMapping("/myFriends")
    public JSONResult myFriends(String userId) throws Exception {

        if (StringUtils.isBlank(userId)){
            return JSONResult.errorMsg("缺少参数");
        }

        List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);

        return JSONResult.ok(myFriends);
    }

    /**
     * 获取未签收的消息列表
     */
    @PostMapping("/getUnReadMsgList")
    public JSONResult getUnReadMsgList(String acceptUserId) throws Exception {

        if (StringUtils.isBlank(acceptUserId)){
            return JSONResult.errorMsg("缺少参数");
        }

        List<ChatMsg> unReadMsgList = userService.queryUnReadMsgList(acceptUserId);
        return JSONResult.ok(unReadMsgList);

    }


}
