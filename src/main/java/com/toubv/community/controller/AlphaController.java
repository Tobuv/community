package com.toubv.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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

}
