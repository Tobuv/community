package com.toubv.community.controller;


import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.sun.javafx.binding.StringFormatter;
import com.toubv.community.common.annotation.LoginRequired;
import com.toubv.community.common.constant.CommentConstant;
import com.toubv.community.entity.User;
import com.toubv.community.service.FollowService;
import com.toubv.community.service.LikeService;
import com.toubv.community.service.UserService;
import com.toubv.community.util.CommunityUtil;
import com.toubv.community.util.CookieUtil;
import com.toubv.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.Multipart;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.util.Date;

@Controller
@RequestMapping("/user")
public class UserController {


    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${alibaba.cloud.access-key}")
    private String accessId;
    @Value("${alibaba.cloud.secret-key}")
    private String accessKey ;
    @Value("${alibaba.cloud.oss.bucketName}")
    private String bucketName ;
    @Value("${alibaba.cloud.oss.endpoint}")
    private String endpoint;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model) {

        String key = CommunityUtil.generateUUID();
        // host的格式为 bucketname.endpoint
        String host = StringFormatter.concat("https://", bucketName, ".", endpoint).getValue();
        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        // String callbackUrl = "http://88.88.88.88:8888";
        // 每一天产生一个文件夹
        String dir = LocalDate.now().toString() + "/"; // 用户上传文件时指定的前缀,如果是 / 则自动检测为文件夹。

        JSONObject jsonObject = new JSONObject();

        long expireTime = 100;
        long expireEndTime = System.currentTimeMillis() + expireTime * 1000; //过期时间 100 秒
        Date expiration = new Date(expireEndTime);
        // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);
        String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
        byte[] binaryData = new byte[0];
        try {
            binaryData = postPolicy.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = ossClient.calculatePostSignature(postPolicy);

        model.addAttribute("OSSAccessKeyId", accessId);
        model.addAttribute("policy", encodedPolicy);
        model.addAttribute("signature", postSignature);
        model.addAttribute("dir", dir);
        model.addAttribute("key", dir + key);
        model.addAttribute("host", host);
        model.addAttribute("expire", String.valueOf(expireEndTime / 1000));

        return "/site/setting";
    }

    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String filename){
        if(StringUtils.isBlank(filename)){
            return CommunityUtil.getJSONString(1, "文件名不能为空！");
        }
        String host = StringFormatter.concat("https://", bucketName, ".", endpoint).getValue();
        String url = host + "/" + filename;
        userService.updateHeader(hostHolder.get().getId(), url);
        return CommunityUtil.getJSONString(0);
    }

    @GetMapping("/getPolicy")
    @ResponseBody
    public JSONObject getPolicy() {

        // host的格式为 bucketname.endpoint
        String host = StringFormatter.concat("https://", bucketName, ".", endpoint).getValue();
        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        // String callbackUrl = "http://88.88.88.88:8888";
        // 每一天产生一个文件夹
        String dir = LocalDate.now().toString() + "/"; // 用户上传文件时指定的前缀,如果是 / 则自动检测为文件夹。

        JSONObject jsonObject = new JSONObject();

        long expireTime = 100;
        long expireEndTime = System.currentTimeMillis() + expireTime * 1000; //过期时间 100 秒
        Date expiration = new Date(expireEndTime);
        // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 1, 1073741824);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);
        String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
        byte[] binaryData = new byte[0];
        try {
            binaryData = postPolicy.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = ossClient.calculatePostSignature(postPolicy);

        jsonObject.put("OSSAccessKeyId", accessId);
        jsonObject.put("policy", encodedPolicy);
        jsonObject.put("signature", postSignature);
        jsonObject.put("dir", dir);
        jsonObject.put("host", host);
        jsonObject.put("expire", String.valueOf(expireEndTime / 1000));


        return jsonObject;
    }

    @LoginRequired
    @PostMapping("/upload")
    public String upload(MultipartFile headerImg, Model model){
        //空值判断
        if(headerImg == null){
            model.addAttribute("error", "您还没选择照片！");
            return "redirect:/setting";
        }
        //生成文件的随机名
        String suffix = StringUtils.substringAfterLast(headerImg.getOriginalFilename(), ".");
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件测试错误！");
            return "/site/setting";
        }
        String filename = CommunityUtil.generateUUID() + "." + suffix;
        //上传文件
        File file = new File(uploadPath + "/" + filename);
        try {
            headerImg.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器异常",e);
        }
        //更新用户头像信息
        User user = hostHolder.get();
        userService.updateHeader(user.getId(), domain + contextPath + "/user/header/" + filename);
        return "redirect:/index";
    }

    @GetMapping("/header/{filename}")
    public void getHeader(HttpServletResponse response, @PathVariable("filename") String filename){
        //服务器存放路径
        filename = uploadPath + "/" + filename;
        try (
                OutputStream os = response.getOutputStream();
                InputStream fis = new FileInputStream(filename);
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取头像失败：" + e.getMessage());
        }
    }

    @LoginRequired
    @PostMapping("/password")
    public String updatePwd(HttpServletRequest request, String oldPassword, String newPassword, Model model){
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg", "输入不能为空！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg", "输入不能为空！");
            return "/site/setting";
        }
        User user = hostHolder.get();
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        if(user.getPassword().equals(oldPassword)){
            userService.updatePassword(user.getId(), newPassword);
            String ticket = CookieUtil.getCookie(request, "ticket");
            userService.logout(ticket);
            return "/site/login";
        }else {
            model.addAttribute("oldPasswordMsg", "密码输入错误");
            return "/site/setting";
        }
    }

    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        //用户
        model.addAttribute("user", user);
        long likeCount = likeService.findUserLikeCount(userId);
        //点赞的数量
        model.addAttribute("likeCount", likeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, CommentConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(CommentConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //关注状态
        boolean haFollowed = false;
        if(hostHolder.get() != null){
            haFollowed = followService.hasFollowed(hostHolder.get().getId(), CommentConstant.ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", haFollowed);
        return "/site/profile";
    }
}
