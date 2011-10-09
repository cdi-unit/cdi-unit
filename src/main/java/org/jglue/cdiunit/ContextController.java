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

public class ContextController {
	@Inject
	private HttpRequestContext _requestContext;

	@Inject
	private HttpSessionContext _sessionContext;

	@Inject
	private HttpConversationContext _conversationContext;

	public void openRequest(HttpServletRequest request) {
		_requestContext.associate(request);
		_requestContext.activate();
	}

	public void closeRequest() {
		_requestContext.deactivate();
	}

	public void openSession(HttpServletRequest request) {
		_sessionContext.associate(request);
		_sessionContext.activate();
	}

	public void closeSession() {
		_sessionContext.deactivate();
	}

	public void openConversation(HttpServletRequest request) {
		_conversationContext.associate(request);
		_conversationContext.activate();
	}

	public void closeConversation() {
		_conversationContext.deactivate();
	}

}
