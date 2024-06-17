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
package io.github.cdiunit.internal.servlet31;

import io.github.cdiunit.internal.servlet.HttpSessionAware;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;

import java.io.IOException;
import java.util.function.Function;

/**
 * Shamlessly ripped from mockrunner. If mockrunner supports servlet 3.1 https://github.com/mockrunner/mockrunner/issues/4 then this class can extend mockrunner instead.
 *
 * @author Various
 */
public class MockHttpServletRequestImpl extends io.github.cdiunit.internal.servlet30.MockHttpServletRequestImpl implements HttpServletRequest, HttpSessionAware {

	public MockHttpServletRequestImpl(ServletContext servletContext, HttpSession httpSession, Function<byte[], ServletInputStream> inputStreamSupplier) {
		super(servletContext, httpSession, inputStreamSupplier);
	}

	@Override
	public String changeSessionId() {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public long getContentLengthLong() {
		return this.contentLength;
	}

}
