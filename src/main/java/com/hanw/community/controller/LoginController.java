package com.hanw.community.controller;

import com.google.code.kaptcha.Producer;
import com.hanw.community.config.KaptchaConfig;
import com.hanw.community.entity.User;
import com.hanw.community.service.UserService;
import com.hanw.community.util.CommunityConstant;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author hanW
 * @create 2022-08-01 10:20
 */
@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaproduce;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path ="/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage() { return "/site/register"; }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     

    @RequestMapping(path="/register",method=RequestMethod.POST)
    //?????????????????????request?????????????????????user???????????????????????????????????????
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg","???????????????????????????????????????????????????????????????????????????????????????");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMessage"));
            model.addAttribute("passwordMsg",map.get("passwordMessage"));
            model.addAttribute("emailMsg",map.get("emailMessage"));
            return "/site/register";
        }
    }

    //http://localhost:8080/community/activation/101/code
    @RequestMapping(path="/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int activation = userService.activation(userId, code);
        if(activation == ACTIVATION_SUCCESS){
            model.addAttribute("msg","?????????????????????????????????????????????????????????");
            model.addAttribute("target","/login");
        }else if(activation == ACTIVATION_REPEAT){
            model.addAttribute("msg","????????????????????????????????????");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","???????????????????????????????????????");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path="/kaptcha",method=RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //???????????????
        String text = kaptchaproduce.createText();
        BufferedImage image = kaptchaproduce.createImage(text);
        /* ??????
            ??????????????????session
            session.setAttribute("kaptcha",text);
         */
        String kaptchaOwner = CommunityUtil.generateUUID();
        RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        Cookie cookie = new Cookie("kaptcahOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //??????????????????redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        //???????????????????????????
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
        }
    }
    //path?????????????????????????????????method???????????????????????????
    @RequestMapping(path="/login",method=RequestMethod.POST)
    public String login(@CookieValue("kaptcahOwner") String kaptchaOwner, HttpServletResponse response,Model model,String username,String password,String code,boolean rememberme/*,HttpSession session*/){
        /*??????
            String kaptcha = (String)session.getAttribute("kaptcha");
         */
        String kaptcha = null;
        if(StringUtils.isNoneBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(redisKey);
        }

        //equalsIgnoreCase()?????????????????????
        //???????????????
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("codeMsg","?????????????????????");
            return "/site/login";
        }
        //?????????????????????
        int expiredSeconds = rememberme?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            //??????cookie???????????????
            cookie.setPath(contextPath);
            //??????cookie?????????????????????
            cookie.setMaxAge(expiredSeconds);
            //???????????????cookie
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMessage"));
            model.addAttribute("passwordMsg",map.get("passwordMessage"));
            return "/site/login";
        }
    }
    @RequestMapping(path="/logout",method=RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}

