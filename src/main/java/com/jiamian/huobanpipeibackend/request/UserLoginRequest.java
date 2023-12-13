/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/2
 * Time: 14:16
 */
package com.jiamian.huobanpipeibackend.request;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -6141644747053915043L;
    private String userAccount;
    private String userPassword;
}
