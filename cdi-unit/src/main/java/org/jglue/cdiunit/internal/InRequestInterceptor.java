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

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jglue.cdiunit.ContextController;
import org.jglue.cdiunit.InRequestScope;

@Interceptor
@InRequestScope
public class InRequestInterceptor {

	@Inject
	private ContextController contextController;

	@AroundInvoke
	public Object around(InvocationContext ctx) throws Exception {
		try {
			contextController.openRequest();
			return ctx.proceed();
		} finally {
			contextController.closeRequest();
		}
	}
}
