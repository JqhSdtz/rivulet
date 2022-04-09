package org.laputa.rivulet.module.auth.service;

import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.auth.entity.RvUser;
import org.laputa.rivulet.module.auth.repository.UserRepository;
import org.laputa.rivulet.module.auth.session.AuthSessionAccessor;
import org.laputa.rivulet.module.auth.util.PasswordUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author JQH
 * @since 下午 8:53 22/03/29
 */
@Service
public class AuthService {
    @Resource
    private UserRepository userRepository;

    @Resource
    private AuthSessionAccessor authSessionAccessor;

    public Result<RvUser> login(RvUser paramUser) {
        Optional<RvUser> optionalRvUser = userRepository.findByUsername(paramUser.getUsername());
        if (optionalRvUser.isEmpty()) {
            return Result.fail(RvUser.class, "NoUserFound", "未找到用户名为、\"" + paramUser.getUsername() + "\"的用户");
        }
        RvUser rvUser = optionalRvUser.get();
        if (!PasswordUtil.verify(paramUser.getPassword(), rvUser.getPassword())) {
            return Result.fail(RvUser.class, "WrongPassword", "登录密码错误");
        }
        authSessionAccessor.setCurrentUser(rvUser);
        return Result.succeed(rvUser);
    }

    public void logout() {
        authSessionAccessor.invalidate();
    }

}
