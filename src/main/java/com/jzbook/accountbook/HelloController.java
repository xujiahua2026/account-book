package com.jzbook.accountbook;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

import com.jzbook.accountbook.entity.User;
import com.jzbook.accountbook.mapper.UserMapper;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;


@CrossOrigin
@RestController
public class HelloController {

    private final UserMapper userMapper;

    public HelloController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/hello")
    public String hello() {
        return "我的第一个接口跑通了！";
    }

    @GetMapping("/user")
    public Map<String, Object> user() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", 1);
        result.put("username", "test");
        result.put("nickname", "测试用户");
        return result;
    }

    @GetMapping("/dbuser")
    public User dbuser() {
        return userMapper.findById(1L);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();

        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            result.put("code", 400);
            result.put("msg", "用户名和密码不能为空");
            return result;
        }
        if (userMapper.findByUsername(username) != null) {
            result.put("code", 400);
            result.put("msg", "用户名已被注册");
            return result;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));

        String nickname = body.get("nickname");
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname);

        userMapper.insert(user);

        result.put("code", 0);
        result.put("msg", "注册成功");
        return result;
    }
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();

        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            result.put("code", 400);
            result.put("msg", "用户名和密码不能为空");
            return result;
        }

        User user = userMapper.findByUsername(username);
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (user == null || !user.getPassword().equals(md5Password)) {
            result.put("code", 401);
            result.put("msg", "用户名或密码错误");
            return result;
        }

        result.put("code", 0);
        result.put("msg", "登录成功");
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        return result;
    }

}
