package org.laputa.rivulet.module.app.session;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * @author JQH
 * @since 下午 7:58 22/04/03
 */
@Component
public class AppSessionAccessor {
    @Resource
    private HttpSession httpSession;

    public void setInitKeyVerified(boolean isVerified) {
        httpSession.setAttribute("initKeyVerified", isVerified);
    }

    public boolean isInitKeyVerified() {
        return (boolean) httpSession.getAttribute("initKeyVerified");
    }
}
