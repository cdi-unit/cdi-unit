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

import java.lang.reflect.InvocationTargetException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.servlet.HttpContextLifecycle;
import org.jboss.weld.servlet.spi.helpers.AcceptingHttpContextActivationFilter;
import org.jglue.cdiunit.internal.LifecycleAwareRequest;

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
	
	
	@PostConstruct
	public void setup() {
		try {
			lifecycle = new HttpContextLifecycle(BeanManagerProxy.unwrap(beanManager), AcceptingHttpContextActivationFilter.INSTANCE, true, true);
		}
		catch(NoSuchMethodError e) {
			try {
				lifecycle = HttpContextLifecycle.class.getConstructor(BeanManager.class, AcceptingHttpContextActivationFilter.class).newInstance(BeanManagerProxy.unwrap(beanManager), AcceptingHttpContextActivationFilter.INSTANCE);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
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
		currentRequest = new LifecycleAwareRequest(lifecycle, request, currentSession);
		lifecycle.requestInitialized(currentRequest, null);
	}

	/**
	 * Close the currently active request.
	 */
	public void closeRequest() {
		lifecycle.requestDestroyed(currentRequest);
		currentSession = currentRequest.getSession(false);
		currentRequest = null;
	}

	
	
	/**
	 * Close the currently active session.
	 */
	public void closeSession() {
		if(currentRequest != null) {
			currentSession = currentRequest.getSession(false); 
		}
		
		if(currentSession != null) {
			lifecycle.sessionDestroyed(currentSession);
			currentSession = null;	
		}
	}

	


}
