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

import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.Http;
import org.jglue.cdiunit.internal.CdiUnitInitialListener;
import org.jglue.cdiunit.internal.servlet.CdiUnitServlet;
import org.jglue.cdiunit.internal.servlet.LifecycleAwareRequest;
import org.jglue.cdiunit.internal.servlet.MockHttpServletRequestImpl;

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
 * 		contextController.openRequest(); // Start a new request.
 *
 * 		starship.start();
 * 		contextController.closeRequest(); // Close the current request.
 * 	}
 * }
 * </pre>
 * 
 * @author Bryn Cooke
 * @author Lars-Fredrik Smedberg
 */
@ApplicationScoped
public class ContextController {

	private ThreadLocal<HttpServletRequest> requests;

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
	private CdiUnitInitialListener listener;

	@PostConstruct
	void initContext() {

		requests = new ThreadLocal<HttpServletRequest>();
		listener.contextInitialized(new ServletContextEvent(context));
	}

	@PreDestroy
	void destroyContext() {

		listener.contextDestroyed(new ServletContextEvent(context));
		requests = null;
	}

	@Inject
	@CdiUnitServlet
	private Provider<MockHttpServletRequestImpl> requestProvider;

	@Inject
	@Http
	private ConversationContext conversationContext;

	/**
	 * Start a request.
	 * @return The request opened.
	 */
	public HttpServletRequest openRequest() {

		HttpServletRequest currentRequest = requests.get();
		if (currentRequest != null) {
			throw new RuntimeException("A request is already open");
		}

		MockHttpServletRequestImpl request = requestProvider.get();

		if (currentSession != null) {

			request.setSession(currentSession);
			request.getSession();
		}

		currentRequest = new LifecycleAwareRequest(listener, request);
		requests.set(currentRequest);

		listener.requestInitialized(new ServletRequestEvent(context, currentRequest));
		if (!conversationContext.isActive()) {
			conversationContext.activate();
		}

		return currentRequest;
	}
	
	/**
	 * @return Returns the current in progress request or throws an exception if the request was not active
	 */
	public HttpServletRequest currentRequest() {

		HttpServletRequest currentRequest = requests.get();
		if (currentRequest == null) {
			throw new RuntimeException("A request has not been opened");
		}

		return currentRequest;
	}

	/**
	 * Close the currently active request.
	 */
	public void closeRequest() {

		HttpServletRequest currentRequest = requests.get();
		if (currentRequest != null) {

			listener.requestDestroyed(new ServletRequestEvent(context, currentRequest));
			currentSession = currentRequest.getSession(false);
		}

		requests.remove();
	}

	/**
	 * Close the currently active session.
	 */
	public void closeSession() {

		HttpServletRequest currentRequest = requests.get();
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
