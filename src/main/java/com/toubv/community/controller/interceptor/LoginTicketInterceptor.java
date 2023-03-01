package com.toubv.community.controller.interceptor;

import com.toubv.community.entity.LoginTicket;
import com.toubv.community.entity.User;
import com.toubv.community.service.UserService;
import com.toubv.community.util.CookieUtil;
import com.toubv.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie获取凭证
        String ticket = CookieUtil.getCookie(request, "ticket");
        if(ticket != null){
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicketByTicket(ticket);
            //验证凭证
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //获取登录信息
                User loginUser = userService.findUserById(loginTicket.getUserId());
                //持有用户信息
                hostHolder.set(loginUser);
                //构建用户认证结果，并存入security，以便于授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        loginUser, loginUser.getPassword(), userService.getAuthorities(loginUser.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User loginUser = hostHolder.get();
        if(loginUser != null && modelAndView != null){
            modelAndView.addObject("loginUser", loginUser);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        //SecurityContextHolder.clearContext();
    }
}
