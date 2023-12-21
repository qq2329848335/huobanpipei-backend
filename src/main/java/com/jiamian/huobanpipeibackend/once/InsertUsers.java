/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/19
 * Time: 13:55
 */
package com.jiamian.huobanpipeibackend.once;
import java.time.LocalDateTime;

import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * springboot程序启动时,批量导入用户
 */
//@Component 这里注释掉,不然每次启动springboot就插入一次数据
public class InsertUsers {
    @Resource
    UserService userService;

    // 使用@Scheduled注解来配置任务的触发条件
    @Scheduled(fixedDelay = Long.MAX_VALUE)// 用一个非常大的延迟值，确保只执行一次
    public void importUser(){
        ArrayList<User> userList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i=0;i<=1000;i++){
            User user = new User();
            user.setUsername("加棉");
            user.setUserAccount(String.valueOf(i));
            user.setUserPassword("12345678");
            user.setGender(0);
            user.setPhone("12345678900");
            user.setEmail("xx@qq.com");
            user.setAvatarUrl("https://th.bing.com/th/id/R.b12afd94326933f64fbf921502ee1e0f?rik=e%2fzbp9RvFBnmRw&pid=ImgRaw&r=0");
            user.setProfile("个人简介。。。");
            user.setTags("");
            userList.add(user);
        }
        userService.saveBatch(userList,100);
        stopWatch.stop();
        System.out.println("耗时: "+stopWatch.getTotalTimeMillis());
    }

}
