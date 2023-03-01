package com.toubv.community.controller.advice;


import com.toubv.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice
public class ExceptionAdvice {

    public static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void ExceptionHandler(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //记录日志
        logger.error("服务器出现异常" + e.getMessage());
        for(StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }

        //分情况：1.普通请求 2.异步请求
        String header = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(header)){
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常"));
        }else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
