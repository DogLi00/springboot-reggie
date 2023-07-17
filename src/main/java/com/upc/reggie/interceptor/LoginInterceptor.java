package com.upc.reggie.interceptor;

import com.alibaba.fastjson.JSON;
import com.upc.reggie.common.R;
import com.upc.reggie.common.ThreadLocalUtils;
import com.upc.reggie.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long employeeId = (Long) request.getSession().getAttribute("employee");
        if (employeeId!=null){
            ThreadLocalUtils.setCurrentId(employeeId);
            log.info("请求已放行，{}",request.getRequestURL());
            return true;
        }
        Long userId = (Long) request.getSession().getAttribute("user");
        if (userId!=null){
            ThreadLocalUtils.setCurrentId(userId);
            log.info("请求已放行，{}",request.getRequestURL());
            return true;
        }
        //response.sendRedirect(request.getContextPath()+"/backend/page/login/login.html");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("请求被拦截，{}",request.getRequestURL());
        return false;
    }
}
