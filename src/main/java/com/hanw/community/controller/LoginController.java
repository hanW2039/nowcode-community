package com.hanw.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author hanW
 * @create 2022-08-01 10:20
 */
@Controller
public class LoginController {

    @RequestMapping(path ="/register",method = RequestMethod.GET)
    public String getRegisterPage() {

        return "/site/register" ;
    }
}
