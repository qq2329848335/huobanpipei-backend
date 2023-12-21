/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/20
 * Time: 16:25
 */
package com.jiamian.huobanpipeibackend.service;
import java.time.LocalDateTime;

import com.jiamian.huobanpipeibackend.model.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest("com.jiamian.huobanpipeibackend")
public class RedisTest {

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;


    @Test
    public void test(){
        //操作string类型的 操作对象
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("jiamian_string","str123");
        valueOperations.set("jiamian_Integer",1);
        valueOperations.set("jiamian_Double",2.0);
        User user = new User();
        user.setId(0L);
        user.setUsername("加棉");
        valueOperations.set("jiamian_User",user);

        //查
        Object jiamian_string = valueOperations.get("jiamian_string");
        Object jiamian_Integer = valueOperations.get("jiamian_Integer");
        Object jiamian_Double = valueOperations.get("jiamian_Double");
        Object jiamian_User = valueOperations.get("jiamian_User");
        System.out.println(jiamian_string);
        System.out.println(jiamian_Integer);
        System.out.println(jiamian_Double);
        System.out.println(jiamian_User);
    }
}
