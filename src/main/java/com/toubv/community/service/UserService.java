package com.toubv.community.service;

import com.toubv.community.common.constant.ActivateConstant;
import com.toubv.community.dao.UserMapper;
import com.toubv.community.entity.User;
import com.toubv.community.util.CommunityUtil;
import com.toubv.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        //控制判断
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "用户名已被占用");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg", "邮箱已被占用");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID());
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮箱
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("url", domain + contextPath +"/activation/" + user.getId() + "/" + user.getActivationCode());

        String content = templateEngine.process("/mail/activation", context);

        mailClient.sendMail(user.getEmail(), "激活您的账号", content);
        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        int status = user.getStatus();
        if(status == 1){
            return ActivateConstant.ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            return ActivateConstant.ACTIVATION_SUCCESS;
        }else {
            return ActivateConstant.ACTIVATION_FAILURE;
        }
    }
}
