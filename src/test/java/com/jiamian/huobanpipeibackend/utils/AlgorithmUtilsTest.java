/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2024/3/4
 * Time: 20:23
 */
package com.jiamian.huobanpipeibackend.utils;

import com.google.gson.Gson;
import jdk.nashorn.internal.parser.TokenType;
import org.junit.Test;
import springfox.documentation.spring.web.json.Json;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AlgorithmUtilsTest {

    @Test
    public void test01(){
        String world1 = "hello";
        String world2 = "he";
        //3
        int i = AlgorithmUtils.minDistance(world1, world2);
        System.out.println(i);
    }

    @Test
    public void test02(){
        String world1 = "['java','大一','篮球']";
        String world2 = "['java','大二','篮球']";
        String world3 = "['java','大三','乒乓球']";
        String world4 = "['python','大四','乒乓球']";
        Gson gson = new Gson();
        ArrayList list1 = gson.fromJson(world1, ArrayList.class);
        ArrayList list2 = gson.fromJson(world2, ArrayList.class);
        ArrayList list3 = gson.fromJson(world3, ArrayList.class);
        ArrayList list4 = gson.fromJson(world4, ArrayList.class);
        int i = AlgorithmUtils.minDistance(list1, list4);
        System.out.println(i);
    }
}
