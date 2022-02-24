package com.bbs.demo.dao;

import com.bbs.demo.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(@Param("userId")int userId,@Param("offset") int offset ,@Param("limit") int limit,@Param("orderModel") int orderModel);

    List<DiscussPost> selectDiscussPostsWithHighOffset(@Param("userId")int userId,@Param("offset") int offset , @Param("limit") int limit, @Param("orderModel") int orderModel);

    List<DiscussPost> selectDiscussPostsWithTag(@Param("userId")int userId,@Param("offset") int offset , @Param("limit") int limit, @Param("orderModel") int orderModel,@Param("tag") String tag);

    int selectDiscussPostRows(@Param("userId")int userId);

    int selectAll();
    int insertDiscussPost(DiscussPost discussPost);
    int insertDiscussPost(@Param("userId")int userId,
                           @Param("title") int title ,
                           @Param("content") int content,
                           @Param("type") int type,
                           @Param("status") int status,
                           @Param("createTime") int createTime,
                           @Param("commentCount") int commentCount,
                           @Param("score") int score
    );
    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(@Param("id")int id,@Param("commentCount")int commentCount);


    int updateType(@Param("id")int id,@Param("type")int type);

    int updateStatus(@Param("id")int id,@Param("status")int status);

    int updateScore(@Param("id")int id,@Param("score")double score);

}
