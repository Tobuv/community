package com.toubv.community.controller.interceptor;

import com.toubv.community.entity.User;
import com.toubv.community.service.MessageService;
import com.toubv.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.get();
        if(user != null && modelAndView != null){
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            int letterUnReadCount = messageService.findLetterUnReadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", noticeUnreadCount + letterUnReadCount);
        }
    }
}
