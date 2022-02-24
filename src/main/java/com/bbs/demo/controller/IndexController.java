package com.bbs.demo.controller;

import com.bbs.demo.dao.DiscussPostMapper;
import com.bbs.demo.entity.DiscussPost;
import com.bbs.demo.entity.Page;
import com.bbs.demo.entity.User;
import com.bbs.demo.service.DiscussPostService;
import com.bbs.demo.service.LikeService;
import com.bbs.demo.service.UserService;
import com.bbs.demo.util.CommunityConstant;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
public class IndexController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    private int count=0;
    @RequestMapping("/test")
    @ResponseBody
    public String sayHello(){
        count++;
        return String.valueOf(count);
    }
    @RequestMapping({"/","/index"})
    public String getIndexPage(Model model, Page page, @RequestParam(name="orderModel",defaultValue = "0") int orderModel,
                               @RequestParam(name="tag",defaultValue = "") String tag,
                               HttpServletResponse resp){
        //在方法调用之前，springmvc会自动实例化Model和Page，并且将Page注入到Model中
        //所以在thymeleaf中可以直接访问到page对象的数据

        Cookie cookie=new Cookie("pageNum","1");
        resp.addCookie(cookie);

        page.setRows(discussPostService.findDiscussPostRows(0));

        //page.setLimit(5);
        List<DiscussPost> list=null;
        if(page.getOffset()<20000) {
             list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderModel,tag.length()==0?null:tag);
        }
        else {
             list = discussPostService.findDiscussPostsWithHighOffset(0, page.getOffset(), page.getLimit(), orderModel);
        }

        List<Map<String,Object>> discussPosts=new ArrayList<>();

        if(list!=null){
            for(DiscussPost post:list){

                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());

                map.put("user",user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderModel",orderModel);
        return "index";
    }

    @RequestMapping("/index2")
    public String toPage(Model model){
        return "index2";
    }


}
