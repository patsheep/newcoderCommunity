package com.bbs.demo.dao;


import com.bbs.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    List<User> selectExceptAdmin();

    int insertUser(User user);

    int updateStatus(@Param("id")int id, @Param("status")int status);

    int updateHeader(@Param("id")int id, @Param("header_url")String headerUrl);

    int updatePassword(int id, String password);

}
