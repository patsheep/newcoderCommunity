import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.bbs.demo.DemoApplication;
import com.bbs.demo.controller.IndexController;
import com.bbs.demo.dao.DiscussPostMapper;
import com.bbs.demo.dao.UserMapper;
import com.bbs.demo.dao.elasticsearch.DiscussPostRepository;
import com.bbs.demo.entity.DiscussPost;
import com.bbs.demo.entity.User;
import com.bbs.demo.service.DiscussPostService;
import com.bbs.demo.service.UserService;
import com.bbs.demo.util.MailClient;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class test {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private UserMapper userMapper;
    @Test
    public void testSelect(){
        //System.out.println("xx");
      //  System.out.println(discussPostMapper.selectDiscussPosts(0,0,10).size());
        //System.out.println(userMapper.selectById(1).toString());
        IndexController x=new IndexController();
        //x.getIndexPage();
        //discussPostMapper.selectDiscussPostRows(0);
    }
    @Autowired
    MailClient mailClient;
    @Autowired
    TemplateEngine templateEngine;
    @Test
    public void testPage(){
        Context context=new Context();
        context.setVariable("username","patchy");
        String content =templateEngine.process("/mails/demo",context);
        mailClient.sendMail("1099178660@qq.com","HTML",content);
       // System.out.println(content);
    }

    @Autowired
    private UserService userService;
    @Test
    public void serviceTest(){
        //User user = userMapper.selectById(6);
        userMapper.updateStatus(6,1);
        //System.out.println(user);

    }
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private  DiscussPostRepository discussPostRepository;
    @Test
    public void testES() throws IOException {
        //System.out.println("!!!");
        //System.out.println(discussPostMapper.selectDiscussPostById(7));
        //CreateIndexRequest request=new CreateIndexRequest("test");
        //CreateIndexResponse createIndexResponse= restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        //System.out.println(createIndexResponse);
         discussPostRepository.save(discussPostMapper.selectDiscussPostById(7));

    }
    @Test
    public void testGetIndex() throws IOException{
        GetIndexRequest request=new GetIndexRequest("test");
        boolean flag=restHighLevelClient.indices().exists(request,RequestOptions.DEFAULT);
        System.out.println(flag);
    }
    @Test
    public void testAddDoc() throws IOException {
        DiscussPost discussPost=new DiscussPost();
        //创建请求
        IndexRequest request=new IndexRequest("test_index");

        //规则 put/kuang_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");

        //将我们的数据放入请求 json
        IndexRequest res=request.source(JSON.toJSONString(discussPost), XContentType.JSON);

        //  客户端发送请求,获得相应结果
        IndexResponse indexResponse=restHighLevelClient.index(request,RequestOptions.DEFAULT);

        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status());
    }

    @Test
    public void insertDiscusspost(){
        Date stDate=new Date();
        for(int i=200001;i<=1000000;i++){
         DiscussPost post=new DiscussPost();
            post.setUserId(8);
            post.setTitle("ContentNo:"+(i+1));
            post.setContent("TestNo:"+(i+1));
            post.setType(0);
            post.setStatus(0);
            post.setCreateTime(new Date());
            discussPostMapper.insertDiscussPost(post);
        }
        Date edDate=new Date();
        System.out.println(edDate.getTime()-stDate.getTime());
    }
    @Autowired
    private DiscussPostService discussPostService;
    @Test
    public void testInsertDate(){//15640

          int mid=20000;

/*        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(simpleDateFormat.format(new Date())+"!!!");
        discussPostMapper.selectDiscussPostsWithHighOffset(0,0,10,0);*/
        Date startTime=new Date();
        //List<DiscussPost> list = discussPostService.findDiscussPosts(0,mid,10,0);
        Date edTime=new Date();
        long val1=edTime.getTime()-startTime.getTime();
       System.out.println("firstVersion:"+(edTime.getTime()-startTime.getTime()));
        startTime=new Date();
        List<DiscussPost> list2 =discussPostService.findDiscussPostsWithHighOffset(0,mid,10,0);
        edTime=new Date();

        System.out.println("SecondVersion:"+(edTime.getTime()-startTime.getTime()));

    }
    @Value("${oss.endpoint}")
     private String endpoint;
    @Value("${oss.accesskeyid}")
     private String accessKeyId;
    @Value("${oss.accesskeysecret}")
        private String accessKeySecret;
    @Value("${oss.bucketName}")
        private String bucketName;
    @Test
    public void putFile(){


        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);


        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, "img/exampleobject.txt", new File("D:\\data.txt"));


        ossClient.putObject(putObjectRequest);


        ossClient.shutdown();

    }

    @Test
    public void getFile(){
        String objectName = "img/exampleobject.txt";
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 下载Object到本地文件，并保存到指定的本地路径中。如果指定的本地文件存在会覆盖，不存在则新建。
        // 如果未指定本地路径，则下载后的文件默认保存到示例程序所属项目对应本地路径中。
        ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File("D:\\CF\\examplefile.txt"));
        //File file=new File();
        Date expiration=new Date(new Date().getTime()+3600*1000);
        URL url=ossClient.generatePresignedUrl(bucketName,objectName,expiration);
        System.out.println(url.toString());
        // 关闭OSSClient。
        ossClient.shutdown();
    }

    @Test
    public void testgetAll(){
        List<User> res=userMapper.selectExceptAdmin();
        for(User i:res){
            System.out.println(i.getId());
        }
    }
}
