package com.bbs.demo.util;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmoticonUtil {
    private final String basePath="https://pathcystore.oss-cn-shanghai.aliyuncs.com/emoticon/";
    private final String reshape="?x-oss-process=image/resize,m_fixed,h_20,w_20";
    private Map<String,String>map;
    @PostConstruct
    //初始化
    public void init() {
        map=new HashMap<>();
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("memeMap.txt");

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;

            while ((keyword = reader.readLine()) != null) {
                System.out.println(keyword);
                String[] split=keyword.split(" ");
                map.put(split[0],htmlTag(basePath+split[1]+reshape));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String htmlTag(String s){
        return "<img src=\""+s+"\">";
    }

    public String translate(String str){
        StringBuffer stringBuffer=new StringBuffer();
        StringBuffer temp=new StringBuffer();
        boolean flag=false;
        for(int i=0;i<str.length();i++){
            char ch=str.charAt(i);
            if(ch=='['){
                flag=true;
                continue;
            }
            if(ch==']'){
                flag=false;
                String path="-";

                if(temp.length()!=0){
                    if(temp.length()>6 && temp.substring(0,6).equals("https:")){
                        System.out.println("getPic");

                        path=htmlTag(temp.toString());

                    }
                    else{
                        path=map.getOrDefault(temp.toString(),"-");
                    }
                }
                if(path.equals("-")){
                    stringBuffer.append("["+temp.toString()+"]");
                }
                else{
                    stringBuffer.append(path);
                }
                temp.setLength(0);

                continue;
            }
            if(!flag){
                stringBuffer.append(ch);
            }
            else{
                temp.append(ch);
            }
        }
        return stringBuffer.toString();
    }

}
