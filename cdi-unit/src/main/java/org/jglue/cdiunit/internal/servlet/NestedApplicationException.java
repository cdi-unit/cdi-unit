package org.jglue.cdiunit.internal.servlet;

import java.io.IOException;

public class NestedApplicationException extends RuntimeException {

	public NestedApplicationException(IOException e) {
		super(e);
	}

}
