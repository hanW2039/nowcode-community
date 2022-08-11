package com.hanw.community.controller;

import com.hanw.community.entity.Message;
import com.hanw.community.entity.Page;
import com.hanw.community.entity.User;
import com.hanw.community.service.MessageService;
import com.hanw.community.service.UserService;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.HostHolder;
import com.sun.xml.internal.ws.api.policy.ModelUnmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author hanW
 * @create 2022-08-10 13:00
 */
@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @RequestMapping(path="/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffSet(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message message : conversationList){
                Map<String,Object> map = new HashMap();
                map.put("conversation",message);
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                int targetId = user.getId() == message.getFromId()? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
            model.addAttribute("conversations",conversations);
            //查询未读消息数量
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
            model.addAttribute("letterUnreadCount",letterUnreadCount);
        }
        return "/site/letter";
    }

    @RequestMapping(path="/letter/detail/{conversationId}",method=RequestMethod.GET)
    public String getLetterDetail(Model model, @PathVariable("conversationId") String conversationId,Page page){
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffSet(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList != null){
            for(Message message : letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target",getLetterTarget(conversationId));

        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId){
        String[] s = conversationId.split("_");
        int id0 = Integer.parseInt(s[0]);
        int id1 = Integer.parseInt(s[1]);

        if(hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else{
            return userService.findUserById(id0);
        }
    }

    @RequestMapping(path="/letter/send",method=RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message letter = new Message();
        letter.setContent(content);
        letter.setCreateTime(new Date());
        letter.setStatus(0);
        int userId = hostHolder.getUser().getId();
        String conversationId = null;
        if(userId > target.getId()){
            conversationId = target.getId() + "_" + userId;
        }else{
            conversationId = userId + "_" + target.getId();
        }
        letter.setConversationId(conversationId);
        letter.setFromId(userId);
        letter.setToId(target.getId());
        messageService.addMessage(letter);
        return CommunityUtil.getJSONString(0);
    }

    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if(letterList != null){
            for(Message message : letterList){
                if(hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }
}




