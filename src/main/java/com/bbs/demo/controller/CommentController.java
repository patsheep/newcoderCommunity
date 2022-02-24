package com.bbs.demo.controller;

import com.bbs.demo.annotation.LoginRequired;
import com.bbs.demo.entity.Comment;
import com.bbs.demo.entity.DiscussPost;

import com.bbs.demo.entity.Event;
import com.bbs.demo.event.EventProducer;
import com.bbs.demo.service.CommentService;
import com.bbs.demo.service.DiscussPostService;
import com.bbs.demo.util.CommunityConstant;
import com.bbs.demo.util.EmoticonUtil;
import com.bbs.demo.util.HostHolder;

import com.bbs.demo.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;


    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;





    //添加评论
    @LoginRequired
    @RequestMapping(value = "/add/{discussPostId}" ,method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){

        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event=new Event().setTopic(TOPIC_COMMENT)
                                .setUserId(hostHolder.getUser().getId())
                                .setEntityType(comment.getEntityType())
                                .setEntityId(comment.getEntityId())
                                .setData("postId",discussPostId);

        if(comment.getEntityType()== ENTITY_TYPE_POST){
            DiscussPost target=discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType()==ENTITY_TYPE_COMMENT){
            Comment target=commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());

        }
        eventProducer.fireEvent(event);
        //触发发帖事件
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            event=new Event().setTopic(TOPIC_PUBLISH).setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST).setEntityId(discussPostId);
            eventProducer.fireEvent(event);
            //计算帖子的分数
            String redisKey= RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }


        return "redirect:/discuss/detail/"+discussPostId;
    }



}
