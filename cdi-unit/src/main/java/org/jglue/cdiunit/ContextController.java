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


import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.servlet.WeldListener;
import org.jglue.cdiunit.internal.CdiUnitServlet;
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
	
	@Inject
	private WeldListener listener;


	private HttpSession currentSession;
	
	
	@Inject
	@CdiUnitServlet
	private Instance<Object> instance;
	
	private ServletContext context;
	
	@PostConstruct
	void initContext() {
		for(Iterator<Object> pos = instance.iterator(); pos.hasNext(); ) {
			Object o = pos.next();
			if(o instanceof ServletContext) {
				context = (ServletContext) o;
			}
		}
		listener.contextInitialized(new ServletContextEvent(context));
	}
	
	@PreDestroy
	void destroyContext() {
		listener.contextDestroyed(new ServletContextEvent(context));
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
		
		currentRequest = new LifecycleAwareRequest(this, request, currentSession);
		listener.requestInitialized(new ServletRequestEvent(context, currentRequest));
		
	}

	/**
	 * Close the currently active request.
	 */
	public void closeRequest() {
		if (currentRequest != null) {
            listener.requestDestroyed(new ServletRequestEvent(context, currentRequest));
            currentSession = currentRequest.getSession(false);
        }
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
			listener.sessionDestroyed(new HttpSessionEvent(currentSession));
			currentSession = null;	
		}
	}

	public void sessionCreated(HttpSession session) {
		currentSession = session;
		listener.sessionCreated(new HttpSessionEvent(currentSession));
	}

	


}
