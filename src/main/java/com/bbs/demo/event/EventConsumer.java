package com.bbs.demo.event;

import com.alibaba.fastjson.JSONObject;

import com.bbs.demo.entity.DiscussPost;
import com.bbs.demo.entity.Event;
import com.bbs.demo.entity.Message;
import com.bbs.demo.entity.User;
import com.bbs.demo.service.DiscussPostService;
import com.bbs.demo.service.ElasticsearchService;
import com.bbs.demo.service.MessageService;
import com.bbs.demo.service.UserService;
import com.bbs.demo.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger=LoggerFactory.getLogger(EventConsumer.class);
    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private UserService userService;



    @KafkaListener(topics={TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public  void handleCommentMessage(ConsumerRecord consumerRecord){
        if(consumerRecord ==null || consumerRecord.value()==null){
            logger.error("消息内容为空");
            return;
        }
        Event event=JSONObject.parseObject(consumerRecord.value().toString(),Event.class);
        System.out.println("event:"+consumerRecord.value().toString());
        if(event == null){
            logger.error("消息格式错误");
            return;
        }
        //发站内通知
        Message message=new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> map=new HashMap<>();
        map.put("userId",event.getUserId());
        map.put("entityType",event.getEntityType());
        map.put("entityId",event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(map));

        System.out.println(map);
        messageService.addMessage(message);

    }
    //发布通知事件
    @KafkaListener(topics = {TOPIC_NOTICE})
    public  void handleNotice(ConsumerRecord consumerRecord){
        System.out.println("runhandleNotice");
        if(consumerRecord ==null || consumerRecord.value()==null){
            logger.error("消息内容为空");
            return;
        }
        Event event=JSONObject.parseObject(consumerRecord.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式错误");
            return;
        }
        //发站内通知
        List<User> userList=userService.selectAllExceptAdmin();
        Date nowDate=new Date();
        for(User user:userList){
            int userId=user.getId();
            Message message=new Message();

            message.setFromId(SYSTEM_USER_ID);
            message.setToId(userId);
            message.setConversationId(event.getTopic());
            message.setCreateTime(nowDate);

            Map<String,Object> map=new HashMap<>();
            map.put("notice",event.getData().get("notice"));
            message.setContent(JSONObject.toJSONString(map));

            System.out.println(map.toString()+" "+map.get("notice"));
            messageService.addMessage(message);
        }

    }
    //消费发帖的事件，往ES里面存数据
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息的格式错误");
            return;
        }
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    //消费删帖的事件，往ES里面存数据
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息的格式错误");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }



}
