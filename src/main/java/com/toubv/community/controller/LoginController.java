package com.toubv.community.controller;

import com.google.code.kaptcha.Producer;
import com.sun.org.apache.xml.internal.dtm.DTMDOMException;
import com.toubv.community.common.constant.ActivateConstant;
import com.toubv.community.common.constant.RememberConstant;
import com.toubv.community.dao.UserMapper;
import com.toubv.community.entity.User;
import com.toubv.community.service.UserService;
import com.toubv.community.util.CommunityUtil;
import com.toubv.community.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer defaultKaptcha;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("{server.servlet.context-path}")
    private String path;

    @GetMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    @PostMapping("/register")
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg", "????????????????????????????????????????????????????????????????????????????????????");
            model.addAttribute("url", "/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/register";
        }
    }

    @GetMapping("/login")
    public String login(){
        return "/site/login";
    }

    @PostMapping("/login")
    public String login(String username, String password, String verifyCode, boolean rememberMe,
                        Model model, HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner){
        //???????????????
        //String kaptcha  =(String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String kaptchaKey = RedisUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(verifyCode) || !kaptcha.equalsIgnoreCase(verifyCode)){
            model.addAttribute("verifyCodeMsg", "??????????????????");
            return "/site/login";
        }
        int expireSeconds = rememberMe ? RememberConstant.REMEMBER_EXPIRED_SECONDS : RememberConstant.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expireSeconds);
        //????????????
        if(map.containsKey("ticket")){
            //???ticket??????cookie???????????????
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(path);
            cookie.setMaxAge(expireSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            //????????????
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            //model.addAttribute("ticketMsg", map.get("ticketMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model,
                             @PathVariable("userId")int userId,
                             @PathVariable("code")String code){

        int result = userService.activation(userId, code);
        if(result == ActivateConstant.ACTIVATION_SUCCESS){
            model.addAttribute("msg", "????????????????????????????????????????????????");
            model.addAttribute("url", "/login");
        }else if(result == ActivateConstant.ACTIVATION_REPEAT){
            model.addAttribute("msg", "??????????????????????????????");
            model.addAttribute("url", "/index");
        }else {
            model.addAttribute("msg", "??????????????????????????????");
            model.addAttribute("url", "/index");
        }
        return "/site/operate-result";
    }
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response){
        //???????????????
        String text = defaultKaptcha.createText();
        BufferedImage image = defaultKaptcha.createImage(text);
        //session?????? -> ???????????????redis
        //session.setAttribute("kaptcha", text);
        String kaptchaOwner = CommunityUtil.generateUUID();
        String kaptchaKey = RedisUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(path);
        response.addCookie(cookie);

        //?????????????????????
        response.setContentType("image/png");

        try (ServletOutputStream os = response.getOutputStream()) {
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
        }
    }
}
