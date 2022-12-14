package com.hanw.community.controller;

import com.hanw.community.annotation.LoginRequired;
import com.hanw.community.entity.Comment;
import com.hanw.community.entity.DiscussPost;
import com.hanw.community.entity.Page;
import com.hanw.community.entity.User;
import com.hanw.community.service.*;
import com.hanw.community.util.CommunityConstant;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.HostHolder;
import com.hanw.community.util.RedisKeyUtil;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.event.FolderAdapter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @author hanW
 * @create 2022-08-05 10:19
 */
@Controller
@RequestMapping(path="/user")
public class UserController implements CommunityConstant {
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
    @Autowired
    private FollowService followService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private CommentService commentService;
    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(path="/setting",method=RequestMethod.GET)
    public String getSettingPage(Model model) {
        //???????????????????????????  ???????????????
        // 1.??????????????????
        String fileName = CommunityUtil.generateUUID();
        // 2.??????????????????
        StringMap policy = new StringMap();
        //?????????????????????{???code??????0}
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        //??????????????????
        Auth auth = Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
        //?????????????????????????????????????????????????????????????????????????????????????????????
        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);
        return "/site/setting";
    }

    // ??????????????????
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "?????????????????????!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    /*
        2022???8???19??? ?????? ??? ???????????????
     */
    @LoginRequired
    @RequestMapping(path="/upload",method=RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","???????????????????????????");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        //suffix == .png(...)
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","?????????????????????!");
            return "/site/setting";
        }
        //?????????????????????
        filename = CommunityUtil.generateUUID()  + suffix;
        //????????????????????????
        File dest = new File(uploadPath + "/" + filename);
        try {
            //????????????
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????" + e.getMessage());
            throw new RuntimeException("????????????????????????????????????",e);
        }
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        User user = hostHolder.getUser();
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }
    /*
       2022???8???19??? ?????? ??? ???????????????
    */
    @RequestMapping(path="/header/{fileName}",method=RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        FileInputStream fis = null;
        //?????????????????????
        fileName = uploadPath + "/" + fileName;
        // ??????????????????
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        //????????????
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
            logger.error("??????????????????" + e.getMessage());
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
            model.addAttribute("oldpwderror","????????????????????????");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPwd)){
            model.addAttribute("newPwderror","?????????????????????");
            return "/site/setting";
        }
        if(newpassword.equals(oldpassword)){
            model.addAttribute("newPwderror","?????????????????????????????????");
            return "/site/setting";
        }
        if(!confirmPwd.equals(newPwd)){
            model.addAttribute("confirmPwderror","??????????????????????????????!");
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
            throw new RuntimeException("??????????????????");
        }
        model.addAttribute("user",user);
        //????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //????????????
        Long followeeCount = followService.followeeCount(userId, EMTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //????????????
        Long followerCount = followService.followerCount(userId,EMTITY_TYPE_USER);
        model.addAttribute("followerCount",followerCount);
        //????????????
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), EMTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

    @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
    public String getMyPostPage(Model model, @PathVariable("userId") int userId, Page page){
        page.setRows(discussPostService.findDiscussPostRows(userId));
        page.setPath("/user/mypost/" + userId);
        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffSet(), page.getLimit(), 0);
        List<Map<String,Object>> myPosts = new ArrayList<>();
        if(list != null){
            for(DiscussPost post : list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                long likeCount = likeService.findEntityLikeCount(EMTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                myPosts.add(map);
            }
        }
        model.addAttribute("myPosts",myPosts);
        int postCount = discussPostService.findDiscussPostRows(userId);
        model.addAttribute("postCount",postCount);
        return "/site/my-post";
    }

    @RequestMapping(path = "/mycomment/{userId}", method = RequestMethod.GET)
    public String getMyCommentPage(Model model, @PathVariable("userId") int userId, Page page){
        page.setRows(commentService.findCommentRowsByUserId(userId));
        page.setPath("/user/mycomment/" + userId);
        List<Comment> list = commentService.findCommentsByUserId(userId, page.getOffSet(), page.getLimit());
        List<Map<String,Object>> myComments = new ArrayList<>();
        if(list != null){
            for(Comment comment : list){
                Map<String,Object> map = new HashMap<>();
                map.put("comment",comment);
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("post",post);
                myComments.add(map);
            }
        }
        model.addAttribute("myComments",myComments);
        long commentCount = commentService.findCommentRowsByUserId(userId);
        model.addAttribute("commentCount",commentCount);
        return "/site/my-reply";
    }
}
