/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/21
 * Time: 14:24
 */
package com.jiamian.huobanpipeibackend.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 数据预热
 */
@Slf4j
@Component
public class PreCacheJob {
    @Resource
    UserService userService;

    @Resource
    RedisTemplate redisTemplate;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    @Scheduled(cron = "0 59 23 ? * ? ")//每天23:59:00执行
    public void doCacheRecommendUser(){
        //查数据库
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        Page<User> userPage = userService.page(new Page<>(1, 20), userQueryWrapper);
        System.out.println("mainUserList = "+mainUserList);
        String redisKey = String.format("yupao:user:recommend:%s",mainUserList.get(0));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //写缓存,60s过期
        try {
            valueOperations.set(redisKey,userPage,24*60*60000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.info("redis set key error",e);
        }

    }
}
