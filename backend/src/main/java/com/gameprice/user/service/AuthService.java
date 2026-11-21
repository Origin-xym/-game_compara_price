package com.gameprice.user.service;

import com.gameprice.user.dto.LoginRequest;
import com.gameprice.user.dto.RegisterRequest;
import com.gameprice.user.vo.LoginVO;
import com.gameprice.user.vo.UserVO;

/**
 * 认证服务接口，提供用户认证相关的功能
 * 包括用户注册、登录、登出以及获取当前登录用户信息等功能
 */
public interface AuthService {

    /** 注册并直接签发 token（AC-8.1「成功并可立即登录」） */
    LoginVO register(RegisterRequest request);

    /** 登录，含防爆破（AC-8.6）与会话时长（Q6 暂定 120 分钟） */
    LoginVO login(LoginRequest request);

    /** 登出：把当前 token 加入黑名单（AC-8.3） */
    void logout(String token);

    /** 当前登录用户信息（AC-8.3 状态切换后前端可重新拉取） */
    UserVO me();
}
