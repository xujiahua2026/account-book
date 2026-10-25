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

    /** 注册：参数校验与重复校验在 Service 层，异常由全局处理器统一返回 */
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
        return result;
    }

    /** 登录：凭证错误抛出 IllegalArgumentException，由全局处理器统一返回 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        User user = userService.login(body.get("username"), body.get("password"));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "登录成功");
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        return result;
    }
}
