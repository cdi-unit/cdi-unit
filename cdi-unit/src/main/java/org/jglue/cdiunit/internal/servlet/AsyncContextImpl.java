/*
 *    Copyright 2014 Bryn Cooke
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
package org.jglue.cdiunit.internal.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class AsyncContextImpl implements AsyncContext {

	public AsyncContextImpl(ServletRequest servletRequest,
			ServletResponse servletResponse) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ServletRequest getRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletResponse getResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispatch() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispatch(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispatch(ServletContext context, String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void complete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(Runnable run) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(AsyncListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(AsyncListener listener,
			ServletRequest servletRequest, ServletResponse servletResponse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends AsyncListener> T createListener(Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTimeout(long timeout) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

}
