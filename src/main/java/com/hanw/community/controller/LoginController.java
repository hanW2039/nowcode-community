package com.hanw.community.controller;

import com.google.code.kaptcha.Producer;
import com.hanw.community.config.KaptchaConfig;
import com.hanw.community.entity.User;
import com.hanw.community.service.UserService;
import com.hanw.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author hanW
 * @create 2022-08-01 10:20
 */
@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaproduce;

    @RequestMapping(path ="/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage() {

        return "/site/register" ;
    }

    @RequestMapping(path="/register",method=RequestMethod.POST)
    //只要页面传值，request中的信息如果和user里的属性对应，就会自动赋值
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
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
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target","/login");
        }else if(activation == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，该账户已激活！");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败，您的激活码错误！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path="/kaptcha",method=RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaproduce.createText();
        BufferedImage image = kaptchaproduce.createImage(text);
        //将验证码存入session
        session.setAttribute("kaptcha",text);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }
}

