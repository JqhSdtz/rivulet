package org.laputa.rivulet.module.auth.controller;

import jakarta.annotation.Resource;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.laputa.rivulet.module.auth.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author JQH
 * @since 下午 4:26 22/04/08
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Resource
    private AuthService authService;

    @PostMapping("/login")
    public Result<RvAdmin> login(@RequestBody @Validated(RvAdmin.Login.class) RvAdmin rvAdmin) {
        return authService.login(rvAdmin);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.succeed();
    }
}
