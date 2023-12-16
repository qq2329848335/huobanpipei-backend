package com.jiamian.huobanpipeibackend.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
public class XingQiuTableUserInfo {
    /**
     * 强制读取第三个 这里不建议 index 和 name 同时用，要么一个对象只用index，要么一个对象只用name去匹配
     */
    @ExcelProperty(index = 0)
    private Long planetCode;
    /**
     * 用名字去匹配，这里需要注意，如果名字重复，会导致只有一个字段读取到数据
     */
    @ExcelProperty(value = "成员昵称")
    private String username;
    @ExcelProperty(value = "本月积分")
    private Double integral;
}
