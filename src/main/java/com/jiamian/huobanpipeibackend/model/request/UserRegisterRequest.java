/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/1
 * Time: 19:29
 */
package com.jiamian.huobanpipeibackend.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -7680901223839187159L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
