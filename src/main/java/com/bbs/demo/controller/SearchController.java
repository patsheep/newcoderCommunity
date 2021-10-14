package com.bbs.demo.controller;

import com.bbs.demo.entity.DiscussPost;
import com.bbs.demo.entity.Page;
import com.bbs.demo.service.ElasticsearchService;
import com.bbs.demo.service.LikeService;
import com.bbs.demo.service.UserService;
import com.bbs.demo.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //search？keyword=xxx
    @RequestMapping(path="search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){

        List<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        //聚合数据

        List<Map<String,Object>> discussPosts=new ArrayList<>();
        if(searchResult!=null){
            for(DiscussPost post:searchResult) {
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                map.put("user",userService.findUserById(post.getUserId()));
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);//关键字回显
        page.setPath("/search?keyword="+keyword);
        page.setRows(searchResult==null?0:searchResult.size());

        return "site/search";
    }
}
