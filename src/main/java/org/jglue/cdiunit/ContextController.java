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
