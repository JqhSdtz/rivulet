package org.laputa.rivulet.module.auth.session;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.springframework.stereotype.Component;

/**
 * @author JQH
 * @since 下午 6:36 22/04/06
 */
@Component
public class AuthSessionAccessor {
    @Resource
    private HttpSession httpSession;

    public void setCurrentAdmin(RvAdmin rvAdmin) {
        httpSession.setAttribute("currentAdmin", rvAdmin);
    }
    public RvAdmin getCurrentAdmin() {
        return (RvAdmin) httpSession.getAttribute("currentAdmin");
    }
    public void invalidate() {
        httpSession.invalidate();
    }

    public void test(Object test) {
        System.out.println(test.getClass().getName());
    }
}
