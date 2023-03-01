package com.toubv.community.controller;

import com.sun.javafx.logging.JFRPulseEvent;
import com.toubv.community.entity.Result;
import com.toubv.community.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    /**
     * post请求
     * @param name
     * @param password
     * @return
     */
    @PostMapping("/student")
    @ResponseBody
    public String saveStudent(String name, String password){
        System.out.println(name);
        System.out.println(password);
        return "<h1>success<h1>";
    }

    @GetMapping("/teacher")
    public ModelAndView getTeacher(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "toub");
        mav.addObject("password", "234");
        mav.setViewName("/demo/view");
        return mav;
    }

    @GetMapping("/school")
    public String getSchool(Model model){
        model.addAttribute("name", "合肥工业大学");
        model.addAttribute("password","HFUT");
        return "/demo/view";
    }

    @GetMapping("/cookie/set")
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie生效范围
        cookie.setPath("/community/alpha");
        //设置生存时间(秒)
        cookie.setMaxAge(1000);

        response.addCookie(cookie);

        return "set cookie success";
    }

    @GetMapping("/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        System.out.println(code);
        return "get cookie";
    }

    @GetMapping("/session/set")
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("name", "zhangsan");
        session.setAttribute("age", 18);

        return "set session";
    }

    @GetMapping("/session/get")
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("name"));
        System.out.println(session.getAttribute("age"));

        return "get session";
    }

    @PostMapping("/ajax")
    @ResponseBody
    public Result testAjax(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return Result.success("success");
    }
}
