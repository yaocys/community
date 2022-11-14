package com.example.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author yao 2022/6/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    public void testStrings(){
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey,1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHashes(){
        String redisKey="user";

        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }
    @Test
    public void testLists(){
        String redisKey = "ids";

       /* redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));*/

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,1));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,1));
    }

    @Test
    public void testSet(){
        String redisKey = "teachers";

        redisTemplate.opsForSet().add(redisKey,"张三","李四","王五","赵六");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        // 随机弹出一个值
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testZset(){
        String redisKey="students";

        redisTemplate.opsForZSet().add(redisKey,"唐僧",90);
        redisTemplate.opsForZSet().add(redisKey,"猴子",56);
        redisTemplate.opsForZSet().add(redisKey,"八戒",76);
        redisTemplate.opsForZSet().add(redisKey,"沙僧",83);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        // 统计多少条数据
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"八戒"));
        // 获取分数
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"八戒"));
        // 排名
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,2));
    }

    @Test
    public void test(){
       /* redisTemplate.delete("user");

        System.out.println(redisTemplate.hasKey("user"));*/

        // redisTemplate.expire("test:count",10, TimeUnit.SECONDS);
    }

    @Test
    public void testBoundOperations(){
/*        String RedisKey = "test:count";

        BoundValueOperations<String,Object> operations = redisTemplate.boundValueOps(RedisKey);

        operations.set(1);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();*/

        // 演示编程式事务



         Object obj = redisTemplate.execute(new SessionCallback<Object>() {
             @Override
             public <K,V>Object execute(RedisOperations<K,V> operations) throws DataAccessException {

                 String redisKey = "test:tx";

                 // 启用事务
                 operations.multi();

//                 operations.opsForValue().set(redisKey,3);


                 // 提交事务
                 return operations.exec();
             }
         });

        System.out.println(obj);


        //System.out.println(operations.get());
    }
}
