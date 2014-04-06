/*
 *    Copyright 2011 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jglue.cdiunit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.Http;
import org.jboss.weld.servlet.WeldListener;
import org.jglue.cdiunit.internal.CdiUnitServlet;
import org.jglue.cdiunit.internal.LifecycleAwareRequest;
import org.jglue.cdiunit.internal.MockHttpServletRequestImpl;

/**
 * Use to explicitly open and close Request, Session and Conversation scopes.
 * <p>
 * If you are testing code that runs over several requests then you may want to
 * explicitly control activation and deactivation of scopes. Use
 * ContextController to do this.
 * </p>
 * 
 * <pre>
 * 
 * &#064;RunWith(CdiRunner.class)
 * &#064;AdditionalClasses(RequestScopedWarpDrive.class)
 * class TestStarship {
 * 
 * 	&#064;Inject
 * 	ContextController contextController; // Obtain an instance of the context
 * 											// controller.
 * 
 * 	&#064;Inject
 * 	Starship starship;
 * 
 * 	&#064;Test
 * 	void testStart() {
 * 		contextController.openRequest(new DummyHttpRequest()); // Start a new
 * 																// request.
 * 		starship.start();
 * 		contextController.closeRequest(); // Close the current request.
 * 	}
 * }
 * </pre>
 * 
 * @author Bryn Cooke
 */
@ApplicationScoped
public class ContextController {

	private HttpServletRequest currentRequest;

	@Inject
	private BeanManager beanManager;

	private HttpSession currentSession;

	@Inject
	@CdiUnitServlet
	private ServletContext context;

	@Inject
	@CdiUnitServlet
	private HttpSession session;

	@Inject
	@ApplicationScoped
	private WeldListener listener;

	@PostConstruct
	void initContext() {
		listener.contextInitialized(new ServletContextEvent(context));
	}

	@PreDestroy
	void destroyContext() {
		listener.contextDestroyed(new ServletContextEvent(context));
	}

	@Inject
	@CdiUnitServlet
	private Provider<MockHttpServletRequestImpl> requestProvider;

	@Inject
	@Http
	private ConversationContext conversationContext;

	/**
	 * Start a request.
	 * 
	 * @param request
	 *            The request to make available.
	 */
	public HttpServletRequest openRequest() {
		if (currentRequest != null) {
			throw new RuntimeException("A request is already open");
		}

		MockHttpServletRequestImpl request = requestProvider.get();

		if (currentSession != null) {
			request.setSession(currentSession);
			request.getSession();
		}

		currentRequest = new LifecycleAwareRequest(listener, request);
		listener.requestInitialized(new ServletRequestEvent(context,
				currentRequest));
		if (!conversationContext.isActive()) {
			conversationContext.activate();
		}
		return currentRequest;
	}

	/**
	 * Close the currently active request.
	 */
	public void closeRequest() {
		if (currentRequest != null) {
			listener.requestDestroyed(new ServletRequestEvent(context,
					currentRequest));
			currentSession = currentRequest.getSession(false);
		}
		currentRequest = null;
	}

	/**
	 * Close the currently active session.
	 */
	public void closeSession() {
		if (currentRequest != null) {
			currentSession = currentRequest.getSession(false);
		}

		if (currentSession != null) {
			listener.sessionDestroyed(new HttpSessionEvent(currentSession));
			currentSession = null;
		}
	}

	public HttpSession getSession() {
		return currentSession;
	}

}
