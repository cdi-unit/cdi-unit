package org.jglue.cdiunit;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@ConversationScoped
public class DConversationScoped implements Serializable {
private String _foo;
	
	public String getFoo() {
		return _foo;
	}
	
	public void setFoo(String foo) {
		_foo = foo;
	}

}