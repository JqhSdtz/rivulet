package org.laputa.rivulet.module.auth.controller;

import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.auth.entity.RvUser;
import org.laputa.rivulet.module.auth.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    public Result<RvUser> login(@RequestBody @Validated(RvUser.Login.class) RvUser rvUser) {
        return authService.login(rvUser);
    }
}
