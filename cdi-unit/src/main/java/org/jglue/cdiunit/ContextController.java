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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;
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
 * 	ContextController _contextController; // Obtain an instance of the context controller.
 * 
 * 	&#064;Inject
 * 	Starship _starship;
 * 
 * 	&#064;Test
 * 	void testStart() {
 * 		_contextController.openRequest(new DummyHttpRequest()); // Start a new request.
 * 		_starship.start();
 * 		_contextController.closeRequest(); // Close the current request.
 * 	}
 * }
 * </pre>
 * 
 * @author Bryn Cooke
 */
public class ContextController {
	@Inject
	private HttpRequestContext _requestContext;

	@Inject
	private HttpSessionContext _sessionContext;

	@Inject
	private HttpConversationContext _conversationContext;

	/**
	 * Start a request.
	 * 
	 * @param request
	 *            The request to make available.
	 */
	public void openRequest(HttpServletRequest request) {
		_requestContext.associate(new SessionHolderAwareRequest(request));
		_requestContext.activate();
	}

	/**
	 * Close the currently active request.
	 */
	public void closeRequest() {
		_requestContext.invalidate();
		_requestContext.deactivate();
	}

	/**
	 * Start a session.
	 * 
	 * @param request
	 *            The request object to use as storage.
	 */
	public void openSession(HttpServletRequest request) {
		_sessionContext.associate(new SessionHolderAwareRequest(request));
		_sessionContext.activate();
	}

	/**
	 * Close the currently active session.
	 */
	public void closeSession() {
		_sessionContext.invalidate();
		_sessionContext.deactivate();
	}

	/**
	 * Start a new conversation.
	 * 
	 * @param request
	 *            The request to use as storage.
	 */
	public void openConversation(HttpServletRequest request) {
		_conversationContext.associate(new SessionHolderAwareRequest(request));
		_conversationContext.activate();
	}

	/**
	 * Close the currently active conversation.
	 */
	public void closeConversation() {
		_conversationContext.invalidate();
		_conversationContext.deactivate();
	}

}
