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
package org.jglue.cdiunit.internal.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.jboss.weld.exceptions.UnsupportedOperationException;

/**
 * Shamlessly ripped from mockrunner. If mockrunner supports servlet 3.1 https://github.com/mockrunner/mockrunner/issues/4 then this class can extend mockrunner instead.
 * 
 * @author Various
 * 
 */
@CdiUnitServlet
public class MockHttpServletRequestImpl implements HttpServletRequest
{
    private Map attributes;
    private Map parameters;
    private Vector locales;
    private Map requestDispatchers;
    
    private String method;
    private String authType;
    private Map headers;
    private String contextPath;
    private String pathInfo;
    private String pathTranslated;
    private String queryString;
    private StringBuffer requestUrl;
    private String requestUri;
    private String servletPath;
    private Principal principal;
    private String remoteUser;
    private boolean requestedSessionIdIsFromCookie;
    private String protocol;
    private String serverName;
    private int serverPort;
    private String scheme;
    private String remoteHost;
    private String remoteAddr;
    private Map roles;
    private String characterEncoding;
    private long contentLength;
    private String contentType;
    private List cookies;
    private MockServletInputStream bodyContent;
    private String localAddr;
    private String localName;
    private int localPort;
    private int remotePort;
    private boolean sessionCreated;
    private List attributeListener;
    private boolean isAsyncSupported;
    
    @Inject
    @CdiUnitServlet
	private ServletContext servletContext;
    
    
    @Inject
    @CdiUnitServlet
	private HttpSession session;
    
	private AsyncContextImpl asyncContext;
    
    public MockHttpServletRequestImpl()
    {
        resetAll();
    }

    /**
     * Resets the state of this object to the default values
     */
    public void resetAll()
    {
        attributes = new HashMap();
        parameters = new HashMap();
        locales = new Vector();
        requestDispatchers = new HashMap();
        method = "GET";
        headers = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        requestedSessionIdIsFromCookie = true;
        protocol = "HTTP/1.1";
        serverName = "localhost";
        serverPort = 8080;
        scheme = "http";
        remoteHost = "localhost";
        remoteAddr = "127.0.0.1";
        roles = new HashMap();
        contentLength = -1;
        cookies = null;
        localAddr = "127.0.0.1";
        localName = "localhost";
        localPort = 8080;
        remotePort = 5000;
        sessionCreated = false;
        attributeListener = new ArrayList();
        bodyContent = new MockServletInputStream(new byte[0]);
        isAsyncSupported = false;
    }

    public void addAttributeListener(ServletRequestAttributeListener listener)
    {
        attributeListener.add(listener);
    }
    
    public String getParameter(String key)
    {
        String[] values = getParameterValues(key);
        if (null != values && 0 < values.length)
        {
            return values[0];
        }
        return null;
    }
    
    /**
     * Clears the parameters.
     */
    public void clearParameters()
    {
        parameters.clear();
    }

    public String[] getParameterValues(String key)
    {
        return (String[])parameters.get(key);
    }

    /**
     * Adds a request multivalue parameter.
     * @param key the parameter key
     * @param values the parameters values
     */
    public void setupAddParameter(String key, String[] values)
    {
        parameters.put(key, values);
    }

    /**
     * Adds a request parameter.
     * @param key the parameter key
     * @param value the parameters value
     */
    public void setupAddParameter(String key, String value)
    {
        setupAddParameter(key, new String[] { value });
    }

    public Enumeration getParameterNames()
    {
        Vector parameterKeys = new Vector(parameters.keySet());
        return parameterKeys.elements();
    }

    public Map getParameterMap()
    {
        return Collections.unmodifiableMap(parameters);
    }
    
    public void clearAttributes()
    {
        attributes.clear();
    }

    public Object getAttribute(String key)
    {
        return attributes.get(key);
    }

    public Enumeration getAttributeNames()
    {
        Vector attKeys = new Vector(attributes.keySet());
        return attKeys.elements();
    }

    public void removeAttribute(String key)
    {
        Object value = attributes.get(key);
        attributes.remove(key);
        if(null != value)
        {
            callAttributeListenersRemovedMethod(key, value);
        }
    }

    public void setAttribute(String key, Object value)
    {
        Object oldValue = attributes.get(key);
        if(null == value)
        {
            attributes.remove(key);
        }
        else
        {
            attributes.put(key, value);
        }
        handleAttributeListenerCalls(key, value, oldValue);
    }
    
    public HttpSession getSession()
    {
        return getSession(true);
    }
    
    public HttpSession getSession(boolean create)
    {
        if(!create && !sessionCreated) return null;
        if(create)
        {
            sessionCreated = true;
            if(session instanceof MockHttpSessionImpl)
            {
                if(!((MockHttpSessionImpl)session).isValid())
                {
                    ((MockHttpSessionImpl)session).resetAll();
                }
            }
        }
        return session;
    }

    /**
     * Sets the <code>HttpSession</code>.
     * @param session the <code>HttpSession</code>
     */
    public void setSession(HttpSession session) 
    {
        this.session = session;   
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        RequestDispatcher dispatcher = (RequestDispatcher)requestDispatchers.get(path);
        if(null == dispatcher)
        {
            dispatcher = new MockRequestDispatcher();
            setRequestDispatcher(path, dispatcher);
        }
        return dispatcher;
    }
    
    /**
     * Returns the map of <code>RequestDispatcher</code> objects. The specified path
     * maps to the corresponding <code>RequestDispatcher</code> object.
     * @return the map of <code>RequestDispatcher</code> objects
     */
    public Map getRequestDispatcherMap()
    {
        return Collections.unmodifiableMap(requestDispatchers);
    }
    
    /**
     * Clears the map of <code>RequestDispatcher</code> objects. 
     */
    public void clearRequestDispatcherMap()
    {
        requestDispatchers.clear();
    }
    
    /**
     * Sets a <code>RequestDispatcher</code> that will be returned when calling
     * {@link #getRequestDispatcher} with the specified path. If no <code>RequestDispatcher</code>
     * is set for the specified path, {@link #getRequestDispatcher} automatically creates a
     * new one.
     * @param path the path for the <code>RequestDispatcher</code>
     * @param dispatcher the <code>RequestDispatcher</code> object
     */
    public void setRequestDispatcher(String path, RequestDispatcher dispatcher)
    {
        if(dispatcher instanceof MockRequestDispatcher)
        {
            ((MockRequestDispatcher)dispatcher).setPath(path);
        }
        requestDispatchers.put(path, dispatcher);
    }
    
    public Locale getLocale()
    {
        if(locales.size() < 1) return Locale.getDefault();
        return (Locale)locales.get(0);
    }

    public Enumeration getLocales()
    {
        return locales.elements();
    }
    
    public void addLocale(Locale locale)
    {
        locales.add(locale);
    }
    
    public void addLocales(List localeList)
    {
        locales.addAll(localeList);
    }
    
    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }
    
    public String getAuthType()
    {
        return authType;
    }
    
    public void setAuthType(String authType)
    {
        this.authType = authType;
    }

    public long getDateHeader(String key)
    {
        String header = getHeader(key);
        if(null == header) return -1;
        try
        {
            Date dateValue = new SimpleDateFormat(WebConstants.DATE_FORMAT_HEADER, Locale.US).parse(header);
            return dateValue.getTime();
        }
        catch (ParseException exc)
        {
            throw new IllegalArgumentException(exc.getMessage());
        }
    }

    public String getHeader(String key)
    {
        List headerList = (List)headers.get(key);
        if(null == headerList || 0 == headerList.size()) return null;
        return (String)headerList.get(0);
    }

    public Enumeration getHeaderNames()
    {
        return new Vector(headers.keySet()).elements();
    }

    public Enumeration getHeaders(String key)
    {
        List headerList = (List)headers.get(key);
        if(null == headerList) return new Vector().elements();;
        return new Vector(headerList).elements();
    }

    public int getIntHeader(String key)
    {
        String header = getHeader(key);
        if(null == header) return -1;
        return new Integer(header).intValue();
    }
    
    public void addHeader(String key, String value)
    {
        List valueList = (List) headers.get(key);
        if (null == valueList)
        {
            valueList = new ArrayList();
            headers.put(key, valueList);
        }
        valueList.add(value);
    }
    
    public void setHeader(String key, String value)
    {
        List valueList = new ArrayList();
        headers.put(key, valueList);
        valueList.add(value);
    }
    
    public void clearHeaders()
    {
        headers.clear();
    }
    
    public String getContextPath()
    {
        return contextPath;
    }
    
    public void setContextPath(String contextPath)
    {
        this.contextPath = contextPath;
    }
    
    public String getPathInfo()
    {
        return pathInfo;
    }
    
    public void setPathInfo(String pathInfo)
    {
        this.pathInfo = pathInfo;
    }
    
    public String getPathTranslated()
    {
        return pathTranslated;
    }
    
    public void setPathTranslated(String pathTranslated)
    {
        this.pathTranslated = pathTranslated;
    }
    
    public String getQueryString()
    {
        return queryString;
    }
    
    public void setQueryString(String queryString)
    {
        this.queryString = queryString;
    }
    
    public String getRequestURI()
    {
        return requestUri;
    }
    
    public void setRequestURI(String requestUri)
    {
        this.requestUri = requestUri;
    }
    
    public StringBuffer getRequestURL()
    {
        return requestUrl;
    }
    
    public void setRequestURL(String requestUrl)
    {
        this.requestUrl = new StringBuffer(requestUrl);
    }
    
    public String getServletPath()
    {
        return servletPath;
    }
    
    public void setServletPath(String servletPath)
    {
        this.servletPath = servletPath;
    }
    
    public Principal getUserPrincipal()
    {
        return principal;
    }
    
    public void setUserPrincipal(Principal principal)
    {
        this.principal = principal;
    }
    
    public String getRemoteUser()
    {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser)
    {
        this.remoteUser = remoteUser;
    }

    public Cookie[] getCookies()
    {
        if(null == cookies) return null;
        return (Cookie[])cookies.toArray(new Cookie[cookies.size()]);
    }
    
    public void addCookie(Cookie cookie)
    {
        if(null == cookies)
        {
            cookies = new ArrayList();
        }
        cookies.add(cookie);
    }

    public String getRequestedSessionId()
    {
        HttpSession session = getSession();
        if(null == session) return null;
        return session.getId();
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return requestedSessionIdIsFromCookie;
    }

    public boolean isRequestedSessionIdFromUrl()
    {
        return isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromURL()
    {
        return !requestedSessionIdIsFromCookie;
    }
    
    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdIsFromCookie)
    {
        this.requestedSessionIdIsFromCookie = requestedSessionIdIsFromCookie;
    }

    public boolean isRequestedSessionIdValid()
    {
        HttpSession session = getSession();
        if(null == session) return false;
        return true;
    }

    public boolean isUserInRole(String role)
    {
        if(!roles.containsKey(role)) return false;
        return ((Boolean)roles.get(role)).booleanValue();
    }
    
    public void setUserInRole(String role, boolean isInRole)
    {
        roles.put(role, new Boolean(isInRole));
    }

    public String getCharacterEncoding()
    {
        return characterEncoding;
    }
    
    public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException
    {
        this.characterEncoding = characterEncoding;
    }

    public int getContentLength()
    {
        return (int)contentLength;
    }
    
    public void setContentLength(int contentLength)
    {
        this.contentLength = contentLength;
    }

    public String getContentType()
    {
        return contentType;
    }
    
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public String getProtocol()
    {
        return protocol;
    }
    
    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }
    
    public String getServerName()
    {
        return serverName;
    }
    
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }
    
    public int getServerPort()
    {
        return serverPort;
    }
    
    public void setServerPort(int serverPort)
    {
        this.serverPort = serverPort;
    }
    
    public String getScheme()
    {
        return scheme;
    }
    
    public void setScheme(String scheme)
    {
        this.scheme = scheme;
    }
    
    public String getRemoteAddr()
    {
        return remoteAddr;
    }
    
    public void setRemoteAddr(String remoteAddr)
    {
        this.remoteAddr = remoteAddr;
    }
    
    public String getRemoteHost()
    {
        return remoteHost;
    }
    
    public void setRemoteHost(String remoteHost)
    {
        this.remoteHost = remoteHost;
    }

    public BufferedReader getReader() throws IOException
    {
        return new BufferedReader(new InputStreamReader(bodyContent));
    }
    
    public ServletInputStream getInputStream() throws IOException
    {
        return bodyContent;
    }
    
    public void setBodyContent(byte[] data)
    {
        bodyContent = new MockServletInputStream(data); 
    }
    
    public void setBodyContent(String bodyContent)
    {
        String encoding = (null == characterEncoding) ? "ISO-8859-1" : characterEncoding;
        try
        {
            setBodyContent(bodyContent.getBytes(encoding));
        } 
        catch(UnsupportedEncodingException exc)
        {
            throw new NestedApplicationException(exc);
        }        
    }

    public String getRealPath(String path)
    {
        HttpSession session = getSession();
        if(null == session) return null;
        return session.getServletContext().getRealPath(path);
    } 
    
    public boolean isSecure()
    {
        String scheme = getScheme();
        if(null == scheme) return false;
        return scheme.equals("https");
    }
    
    public String getLocalAddr()
    {
        return localAddr;
    }
    
    public void setLocalAddr(String localAddr)
    {
        this.localAddr = localAddr;
    }

    public String getLocalName()
    {
        return localName;
    }
    
    public void setLocalName(String localName)
    {
        this.localName = localName;
    }

    public int getLocalPort()
    {
        return localPort;
    }
    
    public void setLocalPort(int localPort)
    {
        this.localPort = localPort;
    }

    public int getRemotePort()
    {
        return remotePort;
    }

    public void setRemotePort(int remotePort)
    {
        this.remotePort = remotePort;
    }
    
    public boolean isAsyncSupported()
    {
        return isAsyncSupported;
    }

    public void setAsyncSupported(boolean isAsyncSupported)
    {
        this.isAsyncSupported = isAsyncSupported;
    }

    private void handleAttributeListenerCalls(String key, Object value, Object oldValue)
    {
        if(null != oldValue)
        {
            if(value != null)
            {
                callAttributeListenersReplacedMethod(key, oldValue);
            }
            else
            {
                callAttributeListenersRemovedMethod(key, oldValue);
            }
        }
        else
        {
            if(value != null)
            {
                callAttributeListenersAddedMethod(key, value);
            }
    
        }
    }
    
    private void callAttributeListenersAddedMethod(String key, Object value)
    {
        for(int ii = 0; ii < attributeListener.size(); ii++)
        {
            ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(getServletContext(), this, key, value);
            ((ServletRequestAttributeListener)attributeListener.get(ii)).attributeAdded(event);
        }
    }
    
    private void callAttributeListenersReplacedMethod(String key, Object value)
    {
        for(int ii = 0; ii < attributeListener.size(); ii++)
        {
            ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(getServletContext(), this, key, value);
            ((ServletRequestAttributeListener)attributeListener.get(ii)).attributeReplaced(event);
        }
    }

    private void callAttributeListenersRemovedMethod(String key, Object value)
    {
        for(int ii = 0; ii < attributeListener.size(); ii++)
        {
            ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(getServletContext(), this, key, value);
            ((ServletRequestAttributeListener)attributeListener.get(ii)).attributeRemoved(event);
        }
    }
    
    public ServletContext getServletContext()
    {
        
        return servletContext;
    }


	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		asyncContext = new AsyncContextImpl(this, null);
		return asyncContext;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest,
			ServletResponse servletResponse) throws IllegalStateException {
		asyncContext = new AsyncContextImpl(servletRequest, servletResponse);
		return asyncContext;
	}

	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DispatcherType getDispatcherType() {
		throw new UnsupportedOperationException();
	}

	
	@Override
	public boolean authenticate(HttpServletResponse response)
			throws IOException, ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void login(String username, String password) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void logout() throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		throw new UnsupportedOperationException();
	}

	

	
	
	
}




