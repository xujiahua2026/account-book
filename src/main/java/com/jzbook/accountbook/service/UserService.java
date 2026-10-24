package com.jzbook.accountbook.service;

import com.jzbook.accountbook.entity.User;

/**
 * 用户业务接口
 */
public interface UserService {

    /** 注册，返回新用户；用户名重复或参数为空时抛 IllegalArgumentException */
    User register(String username, String password, String nickname);

    /** 登录，返回用户；凭证错误时抛 IllegalArgumentException */
    User login(String username, String password);
}
