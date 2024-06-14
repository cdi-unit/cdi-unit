/**
 * Copyright DataStax, Inc.
 * <p>
 * Please see the included license file for details.
 */
package io.github.cdiunit.internal.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;

/**
 * Enables us to inject initial listener. This interface has all the same methods as
 * WeldListener/WeldInitialListener, but allows us to avoid coding directly against
 * these interfaces (which moved around between Weld 1.x and Weld 3.x).
 *
 * @author Sean Flanigan
 */
public interface CdiUnitInitialListener {
	void contextInitialized(ServletContextEvent sce);

	void contextDestroyed(ServletContextEvent sce);

	void sessionCreated(HttpSessionEvent event);

	void sessionDestroyed(HttpSessionEvent event);

	void requestDestroyed(ServletRequestEvent event);

	void requestInitialized(ServletRequestEvent event);
}
