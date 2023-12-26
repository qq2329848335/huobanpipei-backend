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
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

    @Resource
    RedissonClient redissonClient;

    // 重点用户的id集合
    private List<Long> mainUserList = Arrays.asList(1L);


    /**
     * 每天执行，预热推荐用户 (单机模式 [没有使用分布式锁])
     * 多台服务器时这种方式会出现多台服务器一起执行任务,需要使用分布式锁
     */
    /*@Scheduled(cron = "0 59 23 ? * ? ")//每天23:59:00执行
    public void doCacheRecommendUser01(){
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
    }*/


    /**
     * 每天执行，预热推荐用户 (分布式模式 [使用redisTemplate操作redis])
     */
    @Scheduled(cron = "0 09 19 * * *")   //自己设置时间测试
    public void doCacheRecommendUser02() {
        RLock lock = redissonClient.getLock("shayu:precachejob:docache:lock");

        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock: " + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    //查数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.userRecommendByDatabase(1, 20, userId);
                    List<User> records = userPage.getRecords();
                    System.out.println(records);
                    records.stream().forEach(user -> {
                        System.out.println(user);
                    });

                    String redisKey = String.format("yupao:user:recommend:%s", userId);
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    //写缓存,30s过期
                    try {
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }

    //

    /**
     * 每天执行，预热推荐用户 (分布式模式  [使用redisClient操作redis] )
     */
    /*@Scheduled(cron = "0 06 19 * * *")   //自己设置时间测试
    public void doCacheRecommendUserBy03(){
        RLock lock = redissonClient.getLock("yupao:user:recommend:preCacheJob");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0,-1,TimeUnit.MICROSECONDS)) {
                //上方lock.tryLock方法的参数解释:  (1)等待超时时间,(2)锁过期时间,(3)时间单位
                //这里将锁过期时间设置为-1,默认就是30秒,
                //设置为-1,是因为可以开启Redisson的看门狗机制,自动为锁续期,避免方法还没执行完锁就已经过期!!!
                System.out.println("抢到锁的线程是: "+Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    //查数据库
                    Page<User> userPage = userService.userRecommendByDatabase(1, 20, userId);
                    System.out.println(userPage);
                    String redisKey = String.format("yupao:user:recommend:%s", userId);
                    RBucket<Object> bucket = redissonClient.getBucket(redisKey);
                    //存入缓存,24小时过期
                    try {
                        bucket.set(userPage,1000*60*60*24,TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.info("redis set key error",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        }finally {
            //确保只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }*/
}
