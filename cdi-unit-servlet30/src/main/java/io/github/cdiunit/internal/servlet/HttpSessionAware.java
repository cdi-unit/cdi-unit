package io.github.cdiunit.internal.servlet;

import javax.servlet.http.HttpSession;

public interface HttpSessionAware {

	/**
	 * Use HTTP session.
	 *
	 * @param session session to use
	 */
	void setSession(HttpSession session);

}
