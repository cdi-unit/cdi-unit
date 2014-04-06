package org.jglue.cdiunit.internal.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MockRequestDispatcher implements RequestDispatcher
{
    private ServletRequest forwardedRequest;
    private ServletResponse forwardedResponse;
    private ServletRequest includedRequest;
    private ServletResponse includedResponse;
    private String path;
    
    /**
     * Sets the path for this <code>RequestDispatcher</code>.
     * @param path the path
     */
    public void setPath(String path)
    {
        this.path = path;
    }
   
    /**
     * Returns the name or path used to retrieve this <code>RequestDispatcher</code>.
     * @return the name or path used to retrieve this <code>RequestDispatcher</code>
     */
    public String getPath()
    {
        return path;
    }

    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        forwardedRequest = request;
        forwardedResponse = response;
    }

    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        includedRequest = request;
        includedResponse = response;
    }
    
    public ServletRequest getForwardedRequest()
    {
        return forwardedRequest;
    }

    public ServletResponse getForwardedResponse()
    {
        return forwardedResponse;
    }

    public ServletRequest getIncludedRequest()
    {
        return includedRequest;
    }

    public ServletResponse getIncludedResponse()
    {
        return includedResponse;
    }
}