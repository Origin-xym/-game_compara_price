package com.gameprice.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求类
 * 用于封装用户注册时的请求参数
 * 使用@Data注解自动生成getter、setter等方法
 */
@Data
public class RegisterRequest {

    /**
     * 用户名字段
     * 不能为空
     * 必须符合4-20位字母、数字或下划线的正则表达式
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{4,20}$", message = "用户名需 4-20 位字母、数字或下划线")
    private String username;

    /**
     * 密码字段
     * 不能为空
     * 必须符合8-32位，且同时包含字母和数字的正则表达式
     * 允许的特殊字符范围是ASCII码表中的21-7E（可打印字符）
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[\\x21-\\x7E]{8,32}$",
            message = "密码需 8-32 位，且同时包含字母和数字")
    private String password;

    /**
     * 昵称字段
     * 可选字段
     * 最大长度为32个字符
     */
    @Size(max = 32, message = "昵称最长 32 个字符")
    private String nickname;
}
