package com.gameprice.user.vo;

import lombok.Data;

import lombok.Data; // 使用Lombok的@Data注解自动生成getter、setter、toString等方法
/**
 * 用户视图对象(Value Object)
 * 用于在前端展示和后端数据交互时使用的数据结构
 */
@Data
public class UserVO {

    private Long id; // 用户ID，唯一标识符
    private String username; // 用户名，用于登录的唯一标识
    private String nickname; // 用户昵称，用于展示的友好名称
    /** 1 普通用户 2 管理员 */
    private Integer role;
}
