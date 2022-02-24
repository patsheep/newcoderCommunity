package com.bbs.demo.controller;

import com.bbs.demo.entity.User;
import com.bbs.demo.service.FollowService;
import com.bbs.demo.service.LikeService;
import com.bbs.demo.service.OSSService;
import com.bbs.demo.service.UserService;
import com.bbs.demo.util.CommunityConstant;
import com.bbs.demo.util.CommunityUtil;
import com.bbs.demo.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;//http://localhost:8080

    @Value("${server.servlet.context-path}")
    private String contextPath;///community

    @Value("${oss.parsePath}")
    private String parsePath;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private OSSService ossService;


    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {

        return "site/setting";
    }


    @RequestMapping(path ="/upload",method =RequestMethod.POST)
    public String upLoadHeader(MultipartFile headerImage,Model model){

        if(headerImage==null){
            model.addAttribute("error","您未选择图片");
            return "site/setting";
        }

        String fileName =headerImage.getOriginalFilename();
        String suffix=fileName.substring(fileName.lastIndexOf('.'));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不对");
            return "site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.generateUUID();
        try {
            File tempFile=File.createTempFile(fileName,suffix);
            headerImage.transferTo(tempFile);
            ossService.putFile(fileName+suffix,tempFile,"img/");
        } catch (IOException e) {
            e.printStackTrace();
        }
/*        //确定文件存放路径
        File dest=new File(uploadPath+"/"+fileName);

        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //更新当前用户头像路径
        User user= hostHolder.getUser();
        String headerUrl = parsePath+fileName+suffix;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    //废弃
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public  void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器的存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片 通过io流将本地的图片输出给浏览器
        response.setContentType("image/" + suffix);

        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }


    @RequestMapping(path="/updatePassword",method = RequestMethod.POST)
    public String updatePassword(Model model,String oldPassword,String newPassWord,String confirmPassword){
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg", "旧密码不能为空");
            return "site/setting";
        }
        if(StringUtils.isBlank(newPassWord)||StringUtils.isBlank(confirmPassword)){
            model.addAttribute("passwordMsg","密码不能为空");
            return "site/setting";
        }
        if(!newPassWord.equals(confirmPassword)){
            model.addAttribute("passwordMsg","两次密码不一致，请重新输入");
            return "site/setting";
        }
        Map<String, Object> map = userService.updatePassword(hostHolder.getUser().getId(), oldPassword, newPassWord);
        if(map.containsKey("updateSuccess")){
            return "site/login";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            return "site/setting";
        }
    }

    @RequestMapping(path="/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hasFollowed=false;
        if(hostHolder.getUser()!=null){
            hasFollowed=followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);

        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "site/profile";
    }



}
