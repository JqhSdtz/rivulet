package org.laputa.rivulet.module.auth.service;

import jakarta.annotation.Resource;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.laputa.rivulet.module.auth.repository.RvAdminRepository;
import org.laputa.rivulet.module.auth.session.AuthSessionAccessor;
import org.laputa.rivulet.module.auth.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author JQH
 * @since 下午 8:53 22/03/29
 */
@Service
public class AuthService {
    @Resource
    private RvAdminRepository rvAdminRepository;
    @Resource
    private AuthSessionAccessor authSessionAccessor;



    public Result<RvAdmin> login(RvAdmin paramAdmin) {
        Optional<RvAdmin> optionalRvAdmin = rvAdminRepository.findByAdminName(paramAdmin.getAdminName());
        if (optionalRvAdmin.isEmpty()) {
            return Result.fail(RvAdmin.class, "NoAdminFound", "未找到用户名为\"" + paramAdmin.getAdminName() + "\"的管理员");
        }
        RvAdmin rvAdmin = optionalRvAdmin.get();
        if (!PasswordUtil.verify(paramAdmin.getPassword(), rvAdmin.getPassword())) {
            return Result.fail(RvAdmin.class, "WrongPassword", "登录密码错误");
        }
        authSessionAccessor.setCurrentAdmin(rvAdmin);
        return Result.succeed(rvAdmin);
    }

    public void logout() {
        authSessionAccessor.invalidate();
    }


}
