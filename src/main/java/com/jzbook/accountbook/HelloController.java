package com.jzbook.accountbook;

import com.jzbook.accountbook.common.Result;
import com.jzbook.accountbook.dto.AuthResponse;
import com.jzbook.accountbook.dto.LoginRequest;
import com.jzbook.accountbook.dto.RegisterRequest;
import com.jzbook.accountbook.entity.User;
import com.jzbook.accountbook.security.TokenService;
import com.jzbook.accountbook.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 * 参数校验交给 @Valid + 注解，错误由全局异常处理器统一转成 400
 */
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

    /** 注册：成功后直接签发 Token，免去再登录一次 */
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req.getUsername(), req.getPassword(), req.getNickname());
        return Result.ok("注册成功", new AuthResponse(
                user.getId(), user.getNickname(), tokenService.create(user.getId())));
    }

    /** 登录：成功签发 Token，后续请求带 Authorization: Bearer &lt;token&gt; */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        User user = userService.login(req.getUsername(), req.getPassword());
        return Result.ok("登录成功", new AuthResponse(
                user.getId(), user.getNickname(), tokenService.create(user.getId())));
    }
}
