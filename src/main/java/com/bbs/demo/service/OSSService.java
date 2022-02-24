package com.bbs.demo.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
@Service
public class OSSService {
    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.accesskeyid}")
    private String accessKeyId;
    @Value("${oss.accesskeysecret}")
    private String accessKeySecret;
    @Value("${oss.bucketName}")
    private String bucketName;

    //private final String baseLocate="img/";
    public  boolean putFile(String fileName,File file,String baseLocate){

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);


       PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, baseLocate+fileName, file);

        ossClient.putObject(putObjectRequest);


        ossClient.shutdown();
        return true;
    }
}
