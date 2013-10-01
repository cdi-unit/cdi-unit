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
package org.jglue.cdiunit.internal;

import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;

import org.jglue.cdiunit.ContextController;
import org.jglue.cdiunit.InRequestScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interceptor
@InRequestScope
public class InRequestInterceptor {
	private static Logger log = LoggerFactory.getLogger(InRequestInterceptor.class);

	@Inject
	private ContextController contextController;

	@Inject
	@CdiUnitRequest
	private Provider<Object> requestProvider;

	@Inject
	private Provider<HttpServletRequest> cdi1Provider;

	@AroundInvoke
	public Object around(InvocationContext ctx) throws Exception {
		try {
			HttpServletRequest httpServletRequest;
			try {
				httpServletRequest = (HttpServletRequest)requestProvider.get();
			}
			catch(UnsatisfiedResolutionException e) {
				httpServletRequest = cdi1Provider.get();
			}
			
			contextController.openRequest(httpServletRequest);
			return ctx.proceed();
		} catch(Exception e) {
			log.error("Failed to open request context. This can occur is you are using cal10n-0.7.4, see http://jira.qos.ch/browse/CAL-29", e);
			throw e;
		}
		finally {
			contextController.closeRequest();
		}
	}
}
