package com.bbs.demo.controller;

import com.bbs.demo.entity.DiscussPost;
import com.bbs.demo.entity.Event;
import com.bbs.demo.entity.User;
import com.bbs.demo.event.EventProducer;
import com.bbs.demo.service.MessageService;
import com.bbs.demo.util.CommunityConstant;
import com.bbs.demo.util.CommunityUtil;
import com.bbs.demo.util.HostHolder;
import com.bbs.demo.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("Notice")
public class NoticeController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;
    @Autowired
    private EventProducer eventProducer;
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody//返回json
    public String addDiscussPost(String content){
        System.out.println("contnet="+content);
        Event event=new Event().setTopic(TOPIC_NOTICE)
                .setUserId(hostHolder.getUser().getId())
                .setData("notice",content);
        eventProducer.fireEvent(event);


        return CommunityUtil.getJsonString(0,"发布成功！");
    }
}
