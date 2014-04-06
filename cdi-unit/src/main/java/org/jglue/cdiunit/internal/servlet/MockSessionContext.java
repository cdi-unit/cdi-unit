package org.jglue.cdiunit.internal.servlet;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * Mock implementation of <code>HttpSessionContext</code>.
 */
public class MockSessionContext implements HttpSessionContext
{
    public Enumeration getIds()
    { 
        return new Vector().elements();
    }

    public HttpSession getSession(String arg0)
    {
        return null;
    }
}