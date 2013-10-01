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
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.servlet.HttpContextLifecycle;
import org.jboss.weld.servlet.spi.helpers.AcceptingHttpContextActivationFilter;
import org.jglue.cdiunit.internal.SessionHolderAwareRequest;

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
	
	private HttpContextLifecycle lifecycle;

	private HttpSession currentSession;
	
	
	@Inject
	private Conversation conversation;
	
	@PostConstruct
	public void setup() {
		
		lifecycle = new HttpContextLifecycle(BeanManagerProxy.unwrap(beanManager), AcceptingHttpContextActivationFilter.INSTANCE);
		lifecycle.setConversationActivationEnabled(true);
	}
	
	
	/**
	 * Start a request.
	 * 
	 * @param request
	 *            The request to make available.
	 */
	public void openRequest(HttpServletRequest request) {
		if(currentRequest != null) {
			throw new RuntimeException("A request is already open");
		}
		currentRequest = new SessionHolderAwareRequest(request, currentSession);
		lifecycle.requestInitialized(currentRequest, null);
	}

	/**
	 * Close the currently active request.
	 */
	public void closeRequest() {
		lifecycle.requestDestroyed(currentRequest);
		currentRequest = null;
	}

	/**
	 * Start a session.
	 * 
	 * @param request
	 *            The request object to use as storage.
	 */
	public void openSession() {
		if(currentSession != null) {
			throw new RuntimeException("A session is already open");
		}
		if(currentRequest == null) {
			throw new RuntimeException("A session can only be created in the context of a request");
		}
		
		currentSession = currentRequest.getSession();
		lifecycle.sessionCreated(currentSession);
	}

	/**
	 * Close the currently active session.
	 */
	public void closeSession() {
		
		lifecycle.sessionDestroyed(currentSession);
		currentSession = null;
	}

	/**
	 * Start a new conversation.
	 * 
	 * @param request
	 *            The request to use as storage.
	 */
	public void openConversation() {
		if(currentRequest == null) {
			throw new RuntimeException("A conversation can only be created in the context of a request");
		}
		conversation.begin();
	}

	/**
	 * Close the currently active conversation.
	 */
	public void closeConversation() {
		conversation.end();
	}



}
