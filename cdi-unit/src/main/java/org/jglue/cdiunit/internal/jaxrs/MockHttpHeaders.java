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
package org.jglue.cdiunit.internal.jaxrs;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;


public class MockHttpHeaders implements HttpHeaders {

	public MockHttpHeaders(HttpServletRequest httpServletRequest) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<String> getRequestHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeaderString(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultivaluedMap<String, String> getRequestHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MediaType> getAcceptableMediaTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Locale> getAcceptableLanguages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MediaType getMediaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Cookie> getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

}
