package com.hanw.community.controller.interceptor;

import com.hanw.community.entity.User;
import com.hanw.community.service.MessageService;
import com.hanw.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author hanW
 * @create 2022-08-14 21:31
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount",letterUnreadCount + unreadNoticeCount);
        }
    }
}
