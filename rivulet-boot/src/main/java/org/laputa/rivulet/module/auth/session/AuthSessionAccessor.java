package org.laputa.rivulet.module.auth.session;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.laputa.rivulet.module.auth.entity.RvUser;
import org.springframework.stereotype.Component;

/**
 * @author JQH
 * @since 下午 6:36 22/04/06
 */
@Component
public class AuthSessionAccessor {
    @Resource
    private HttpSession httpSession;

    public void setCurrentUser(RvUser rvUser) {
        httpSession.setAttribute("currentUser", rvUser);
    }
    public RvUser getCurrentUser() {
        return (RvUser) httpSession.getAttribute("currentUser");
    }
    public void invalidate() {
        httpSession.invalidate();
    }
}
