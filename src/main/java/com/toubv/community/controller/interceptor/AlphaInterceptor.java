package com.toubv.community.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AlphaInterceptor implements HandlerInterceptor {

    public static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);
    //controller方法执行前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("preHandler..." + handler.getClass());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    //controller方法执行前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.debug("postHandler..." + handler.getClass());
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    //模板引擎后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("afterCompleting..." + handler.getClass());
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
