package com.jzbook.accountbook;

import com.jzbook.accountbook.entity.User;
import com.jzbook.accountbook.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class HelloController {

    private final UserService userService;

    public HelloController(UserService userService) {
        this.userService = userService;
    }

    /** 健康检查 */
    @GetMapping("/hello")
    public String hello() {
        return "我的第一个接口跑通了！";
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.register(
                    body.get("username"),
                    body.get("password"),
                    body.get("nickname")
            );
            result.put("code", 0);
            result.put("msg", "注册成功");
            result.put("userId", user.getId());
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.login(body.get("username"), body.get("password"));
            result.put("code", 0);
            result.put("msg", "登录成功");
            result.put("userId", user.getId());
            result.put("nickname", user.getNickname());
        } catch (IllegalArgumentException e) {
            result.put("code", 401);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
