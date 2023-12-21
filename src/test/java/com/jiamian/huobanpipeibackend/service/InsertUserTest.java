/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/19
 * Time: 14:50
 */
package com.jiamian.huobanpipeibackend.service;

import com.jiamian.huobanpipeibackend.model.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 用户插入单元测试，注意打包时要删掉或忽略，不然打一次包就插入一次
 */
@RunWith(SpringRunner.class)
@SpringBootTest("com.jiamian.huobanpipeibackend")
public class InsertUserTest {
    @Resource
    UserService userService;

    //线程设置
    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 线性导入用户   1000  耗时: 4189ms
     */
    @Test
    public void doLinearImportUser() {
        final int INSERT_NUM = 1000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 1; i <= INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("加棉");
            user.setUserAccount("jiamian123");
            user.setUserPassword("12345678");
            user.setGender(0);
            user.setPhone("12345678900");
            user.setEmail("xx@qq.com");
            user.setAvatarUrl("https://th.bing.com/th/id/R.b12afd94326933f64fbf921502ee1e0f?rik=e%2fzbp9RvFBnmRw&pid=ImgRaw&r=0");
            user.setProfile("个人简介。。。");
            user.setTags("");
            userService.save(user);
        }
        stopWatch.stop();
        System.out.println("线性插入" + INSERT_NUM + "条数据,耗时: " + stopWatch.getTotalTimeMillis());

    }

    /**
     * 批量插入用户   1000  耗时： 781ms
     */
    @Test
    public void doBatchImportUser() {
        final int INSERT_NUM = 1000;
        ArrayList<User> userList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 1; i <= INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("加棉");
            user.setUserAccount("jiamian123");
            user.setUserPassword("12345678");
            user.setGender(0);
            user.setPhone("12345678900");
            user.setEmail("xx@qq.com");
            user.setAvatarUrl("https://th.bing.com/th/id/R.b12afd94326933f64fbf921502ee1e0f?rik=e%2fzbp9RvFBnmRw&pid=ImgRaw&r=0");
            user.setProfile("个人简介。。。");
            user.setTags("");
            userList.add(user);
        }
        userService.saveBatch(userList, 100);
        stopWatch.stop();
        System.out.println("线性插入" + INSERT_NUM + "条数据,耗时: " + stopWatch.getTotalTimeMillis());
    }


    /**
     * 并发批量插入用户   100000  耗时： 6204ms
     */
    @Test
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 3000000;
        // 分十组
        int j = 0;
        //批量插入数据的大小
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数。（鱼皮这里直接取了个值，会有问题,我这里随便写的）
        for (int i = 1; i < INSERT_NUM / batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("加棉");
                user.setUserAccount("jiamian123");
                user.setUserPassword("12345678");
                user.setGender(0);
                user.setPhone("12345678900");
                user.setEmail("xx@qq.com");
                user.setAvatarUrl("https://th.bing.com/th/id/R.b12afd94326933f64fbf921502ee1e0f?rik=e%2fzbp9RvFBnmRw&pid=ImgRaw&r=0");
                user.setProfile("个人简介。。。");
                user.setTags("");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getLastTaskTimeMillis());

    }

}
