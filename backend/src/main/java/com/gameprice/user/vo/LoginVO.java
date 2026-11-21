package com.gameprice.user.vo;

import lombok.Data;

import java.util.Date;

/**
 * 登录结果值对象（Value Object）
 * 用于封装登录成功后返回给前端的数据
 */
@Data
public class LoginVO {

    /** 登录令牌，用于后续请求的身份认证 */
    private String token;
    /** token 过期时间，前端可用于提前提示 */
    private Date expiresAt;
    /** 当前登录用户的详细信息 */
    private UserVO user;
}
