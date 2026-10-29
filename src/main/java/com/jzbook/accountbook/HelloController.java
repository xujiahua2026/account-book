package com.jzbook.accountbook;

import com.jzbook.accountbook.entity.User;
import com.jzbook.accountbook.security.TokenService;
import com.jzbook.accountbook.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class HelloController {

    private final UserService userService;
    private final TokenService tokenService;

    public HelloController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /** 健康检查 */
    @GetMapping("/hello")
    public String hello() {
        return "我的第一个接口跑通了！";
    }

    /** 注册：注册成功后直接签发 Token，免去再登录一次 */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        User user = userService.register(
                body.get("username"),
                body.get("password"),
                body.get("nickname")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "注册成功");
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("token", tokenService.create(user.getId()));
        return result;
    }

    /** 登录：成功签发 Token，后续请求带 Authorization: Bearer <token> */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        User user = userService.login(body.get("username"), body.get("password"));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "登录成功");
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("token", tokenService.create(user.getId()));
        return result;
    }
}
