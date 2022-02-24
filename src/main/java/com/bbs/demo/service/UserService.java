package com.bbs.demo.service;

import com.bbs.demo.dao.LoginTicketMapper;
import com.bbs.demo.dao.UserMapper;
import com.bbs.demo.entity.LoginTicket;
import com.bbs.demo.entity.User;
import com.bbs.demo.util.CommunityConstant;
import com.bbs.demo.util.CommunityUtil;
import com.bbs.demo.util.MailClient;
import com.bbs.demo.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired  UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${community.path.domain}")
    private  String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){

        User user=getCache(id);
        if(user==null){
            user=initCache(id);
        }
        return user;
        //return userMapper.selectById(id);
    }

    public void test(){
        System.out.println(domain+" "+contextPath);
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map=new HashMap<>();
        //空值
        if(user == null){
            throw  new IllegalArgumentException("参数为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
        }

        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
        }

        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
        }
        //验证账号
        User u=userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","账号已经存在");

            return map;
        }

        //验证邮箱

        u=userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","邮箱已注册");
            return map;

        }
        //开始注册
        user.setSalt(CommunityUtil.generateUUID().substring(0,8));//生成8位盐值
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        userMapper.insertUser(user);
        //激活邮件
        Context context=new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:8888/demo/activation/101/code
        String url=domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content=templateEngine.process("mails/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    // 激活
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        //已经被激活
        if(user.getStatus()==1){
            return ACTIVIATION_REPEATE;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);//激活账户
            cleanCache(userId);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVIATION_FAILURE;
        }
    }

    //登录 生成登录凭证->session 存到数据库中 1.存到mysql中 利用loginticket
    public Map<String,Object> login(String username,String password,long expiredSeconds){
        Map<String,Object> map=new HashMap<>();
        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        //验证账号
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","该账号尚未激活！");
            return map;
        }
        password=CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码错误");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());//随机生成登录凭证
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
       // loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);
        //带回ticket给页面
        map.put("ticket",loginTicket.getTicket());
        return map;
    }


    public void logOut(String ticket){

        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket =(LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);


    }
    public LoginTicket findLoginTicket(String ticket){
       // return loginTicketMapper.selectByTicket(ticket);
        String redisKey=RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    //更改密码
    public Map<String,Object> updatePassword(int userId,String oldpassWord,String newpassWord){
        Map<String,Object> map=new HashMap<>();
        User user = userMapper.selectById(userId);
        if(user==null){
            map.put("usernameMsg","账号不存在");
            return map;
        }
        oldpassWord=CommunityUtil.md5(oldpassWord+user.getSalt());
        if(!user.getPassword().equals(oldpassWord)){
            map.put("oldPasswordMsg","旧密码错误");
            return map;
        }
        newpassWord=CommunityUtil.md5(newpassWord+user.getSalt());
        userMapper.updatePassword(userId,newpassWord);
        map.put("updateSuccess","修改密码成功");
        return map;
    }
    public int updateHeader(int userId,String headerUrl){
        int res=userMapper.updateHeader(userId,headerUrl);
        cleanCache(userId);
        return res;
        //return userMapper.updateHeader(userId,headerUrl);
    }
    //1.优先从缓存取值
    private User getCache(int userId){
        String redisKey= RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    //2.取不到就初始化缓存数据
    private User initCache(int userId){
        User user =userMapper.selectById(userId);
        String redisKey= RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,1, TimeUnit.HOURS);
        return user;
    }
    //3.数据变更时清除缓存数据
    private void cleanCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);

    }



    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user=this.findUserById(userId);

        List<GrantedAuthority> list=new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }

            }
        });
        return list;
    }

    public List<User> selectAllExceptAdmin(){
        return userMapper.selectExceptAdmin();
    }
}
