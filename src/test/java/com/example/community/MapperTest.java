package com.example.community;

import com.example.community.dao.DiscussPostMapper;
import com.example.community.dao.UserMapper;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.User;
import com.example.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author yao 2022/4/13
 */
@SpringBootTest
public class MapperTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Test
    public void testSelectPost(){
        /*List<DiscussPost> list = discussPostMapper.selectDiscussPosts(101,0,10);
        //int count = discussPostMapper.selectDiscussPostRows(101);
        for(DiscussPost post:list){
            System.out.println(post);
        }*/
        User user = new User();
        user.setId(2);
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("fae312");
        user.setEmail("123.qq.com");
        user.setType(1);
        user.setStatus(1);
        user.setActivationCode(null);
        user.setHeaderUrl(null);
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //userMapper.updateStatus(111,1);
        // User user = userMapper.selectById(111);
        // System.out.println(user);

        // System.out.println("查询到的记录行数："+count);
        // mailClient.sendMail("yaosunique@gmail.com","test","Hello STMP!");
    }
}
