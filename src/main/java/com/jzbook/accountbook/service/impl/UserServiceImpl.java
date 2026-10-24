package com.jzbook.accountbook.service.impl;

import com.jzbook.accountbook.entity.User;
import com.jzbook.accountbook.mapper.UserMapper;
import com.jzbook.accountbook.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User register(String username, String password, String nickname) {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (userMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已被注册");
        }

        User user = new User();
        user.setUsername(username);
        // 密码不可逆加密后入库，数据库不保存明文
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setNickname(isBlank(nickname) ? username : nickname);

        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        User user = userMapper.findByUsername(username);
        String md5 = DigestUtils.md5DigestAsHex(password.getBytes());

        // 用户不存在与密码错误返回同一提示，避免账号枚举
        if (user == null || !user.getPassword().equals(md5)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return user;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
