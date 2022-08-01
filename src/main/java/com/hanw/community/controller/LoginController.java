package com.hanw.community.controller;

import com.hanw.community.entity.User;
import com.hanw.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * @author hanW
 * @create 2022-08-01 10:20
 */
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping(path ="/register",method = RequestMethod.GET)
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
}
