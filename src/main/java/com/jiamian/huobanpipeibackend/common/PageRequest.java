/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/23
 * Time: 18:45
 */
package com.jiamian.huobanpipeibackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页类
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 3348053251694541451L;
    /**
     * 当前页
     */
    protected int pageNum;

    /**
     * 每页大小
     */
    protected int pageSize;
}
