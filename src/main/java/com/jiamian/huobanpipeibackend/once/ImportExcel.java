/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/15
 * Time: 20:03
 */
package com.jiamian.huobanpipeibackend.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.util.ListUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


import java.util.List;
import java.util.Map;

@Slf4j
public class ImportExcel {
    /**
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    @Test
    public void synchronousRead() {
        String fileName = "D:\\develop\\java\\IDEA_2019\\webProject\\ljr-yupi\\huobanpipei\\huobanpipei-backend\\src\\main\\resources\\testExcel.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuTableUserInfo> list = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo data : list) {
            log.info("读取到数据:{}", new Gson().toJson(data));
        }
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        List<Map<Integer, String>> listMap = EasyExcel.read(fileName).sheet().doReadSync();
        for (Map<Integer, String> data : listMap) {
            // 返回每条数据的键值对 表示所在的列 和所在列的值
            log.info("读取到数据:{}",  new Gson().toJson(data));
        }
    }








    /**
     * 最简单的读
     * <p>
     * 1. 创建excel对应的实体对象 参照{@link XingQiuTableUserInfo}
     * <p>
     * 2. 由于默认一行行的读取excel，所以需要创建excel一行一行的回调监听器，参照{@link TableListener}
     * <p>
     * 3. 直接读即可
     */
    @Test
    public void simpleRead() {
        log.info("==========================写法1 不需要创建监听器===========================");
        // 写法1：JDK8+ ,不用额外写一个XingQiuTableUserInfoListener
        // since: 3.0.0-beta1
        //3.0.0版本之后,使用这种方法不需要创建监听器
        String fileName = "D:\\develop\\java\\IDEA_2019\\webProject\\ljr-yupi\\huobanpipei\\huobanpipei-backend\\src\\main\\resources\\testExcel.xlsx";
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new PageReadListener<XingQiuTableUserInfo>(dataList -> {
            for (XingQiuTableUserInfo XingQiuTableUserInfo : dataList) {
                log.info("读取到一条数据{}", new Gson().toJson(XingQiuTableUserInfo));
            }
        })).sheet().doRead();


        log.info("==========================写法2 匿名内部类===========================");
        // 写法2：
        // 匿名内部类(创建一个监听器对象) 不用额外写一个XingQiuTableUserInfoListener
        fileName = "D:\\develop\\java\\IDEA_2019\\webProject\\ljr-yupi\\huobanpipei\\huobanpipei-backend\\src\\main\\resources\\testExcel.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new ReadListener<XingQiuTableUserInfo>() {
            /**
             * 单次缓存的数据量
             */
            public static final int BATCH_COUNT = 100;
            /**
             *临时存储
             */
            private List<XingQiuTableUserInfo> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

            @Override
            public void invoke(XingQiuTableUserInfo data, AnalysisContext context) {
                cachedDataList.add(data);
                if (cachedDataList.size() >= BATCH_COUNT) {
                    saveData();
                    // 存储完成清理 list
                    cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                saveData();
            }

            /**
             * 加上存储数据库
             */
            private void saveData() {
                log.info("{}条数据，开始存储数据库！", cachedDataList.size());
                log.info("存储数据库成功！");
            }
        }).sheet().doRead();


        log.info("==========================写法3 需创建监听器===========================");
        // 有个很重要的点 TableListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
        // 写法3：
        //这种方法需要写一个监听器
        fileName = "D:\\develop\\java\\IDEA_2019\\webProject\\ljr-yupi\\huobanpipei\\huobanpipei-backend\\src\\main\\resources\\testExcel.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();


        log.info("==========================写法4 读单文件多个sheet===========================");
        // 写法4
        //这种方法可以读一个文件里的多个sheet
        fileName = "D:\\develop\\java\\IDEA_2019\\webProject\\ljr-yupi\\huobanpipei\\huobanpipei-backend\\src\\main\\resources\\testExcel.xlsx";
        // 一个文件一个reader
        try (ExcelReader excelReader = EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).build()) {
            // 构建一个sheet 这里可以指定名字或者no
            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            // 读取一个sheet
            excelReader.read(readSheet);
        }



    }
}
