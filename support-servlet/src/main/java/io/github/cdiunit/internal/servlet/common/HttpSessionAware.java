package io.github.cdiunit.internal.servlet.common;

import jakarta.servlet.http.HttpSession;

public interface HttpSessionAware {

    /**
     * Use HTTP session.
     *
     * @param session session to use
     */
    void setSession(HttpSession session);

}
