package com.bbs.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.bbs.demo.entity.User;
import com.bbs.demo.service.FollowService;
import com.bbs.demo.service.LikeService;
import com.bbs.demo.service.OSSService;
import com.bbs.demo.util.CommunityUtil;
import com.bbs.demo.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
@Controller
public class fileController {
    @Value("${oss.imageparsePath}")
    private String parsePath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private OSSService ossService;

    @RequestMapping("/imgUpload")
    @ResponseBody
    public String testupload(@RequestParam(value ="editormd-image-file",required =false) MultipartFile file){

        JSONObject res=new JSONObject();
        if(file==null){
            res.put("success",0);
            res.put("message","fileisempty");
            return  res.toJSONString();
        }


        String fileName =file.getOriginalFilename();
        String suffix=fileName.substring(fileName.lastIndexOf('.'));
        if(StringUtils.isBlank(suffix)){
            res.put("success",0);
            res.put("message","fileformerror");
            return res.toJSONString();
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID();

        try {
            File tempFile=File.createTempFile(fileName,suffix);
            file.transferTo(tempFile);
            ossService.putFile(fileName+suffix,tempFile,"pic/");
            String Url = parsePath+fileName+suffix;
            res.put("success",1);
            res.put("message","success");
            res.put("url",Url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(res.size()==0){
            res.put("success",0);
            res.put("message","error");

        }
        //String res="{success : 1,message :\"success\",url:\"https://static.nowcoder.com/images/logo_87_87.png\"}";
        return res.toJSONString();






    }
}
