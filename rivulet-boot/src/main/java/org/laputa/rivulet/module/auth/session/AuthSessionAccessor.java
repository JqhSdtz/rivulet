package org.laputa.rivulet.module.auth.session;

import org.laputa.rivulet.module.auth.entity.RvUser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

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
