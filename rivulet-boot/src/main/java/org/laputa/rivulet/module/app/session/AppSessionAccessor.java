package org.laputa.rivulet.module.app.session;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

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
