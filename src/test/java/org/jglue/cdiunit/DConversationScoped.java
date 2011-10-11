package org.jglue.cdiunit;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;

@ConversationScoped
public class DConversationScoped implements Serializable {
	private static final long serialVersionUID = 1L;
	private String _foo;

	public String getFoo() {
		return _foo;
	}

	public void setFoo(String foo) {
		_foo = foo;
	}

}