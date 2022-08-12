package com.hanw.community.controller;

import com.hanw.community.annotation.LoginRequired;
import com.hanw.community.entity.User;
import com.hanw.community.service.LikeService;
import com.hanw.community.service.UserService;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.HostHolder;
import com.hanw.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.event.FolderAdapter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * @author hanW
 * @create 2022-08-05 10:19
 */
@Controller
@RequestMapping(path="/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @LoginRequired
    @RequestMapping(path="/setting",method=RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path="/upload",method=RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        //suffix == .png(...)
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","图片格式不正确!");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID()  + suffix;
        //确定文件存放路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常",e);
        }
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        User user = hostHolder.getUser();
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path="/header/{fileName}",method=RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        FileInputStream fis = null;
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 解析文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        //想要图片
        response.setContentType("image/" + suffix);
        try {
            fis = new FileInputStream(fileName);
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b =0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @LoginRequired
    @RequestMapping(path = "/update",method = RequestMethod.POST)
    public String updatePassword(@CookieValue("ticket") String ticket,Model model, String oldPwd, String newPwd, String confirmPwd){
        User user = hostHolder.getUser();
        String oldpassword = oldPwd + user.getSalt();
        oldpassword =CommunityUtil.md5(oldpassword);
        String newpassword = newPwd + user.getSalt();
        newpassword =CommunityUtil.md5(newpassword);

        if(StringUtils.isBlank(oldPwd) || !oldpassword.equals(user.getPassword())){
            model.addAttribute("oldpwderror","原密码错误或为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPwd)){
            model.addAttribute("newPwderror","新密码不能为空");
            return "/site/setting";
        }
        if(newpassword.equals(oldpassword)){
            model.addAttribute("newPwderror","新密码不能与原密码相同");
            return "/site/setting";
        }
        if(!confirmPwd.equals(newPwd)){
            model.addAttribute("confirmPwderror","两次输入的密码不一致!");
            return "/site/setting";
        }
        userService.updatePassword(user.getId(),newpassword);
        userService.logout(ticket);
        return "redirect:/login";
    }

    @RequestMapping(path="/profile/{userId}",method=RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        return "/site/profile";
    }
}
