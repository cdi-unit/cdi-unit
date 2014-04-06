package org.jglue.cdiunit.internal.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

/**
 * Mock implementation of <code>ServletOutputStream</code>.
 */
public class MockServletOutputStream extends ServletOutputStream
{
    private ByteArrayOutputStream buffer;
    private String encoding;
    
    public MockServletOutputStream()
    {
        this("ISO-8859-1");
    }
    
    public MockServletOutputStream(String encoding)
    {
        buffer = new ByteArrayOutputStream();
        this.encoding = encoding;
    }
    
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
    
    public void write(int value) throws IOException
    {
        buffer.write(value);
    }
    
    public String getContent()
    {
        try
        {
            buffer.flush();
            return buffer.toString(encoding);
        } 
        catch(IOException exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
    
    public byte[] getBinaryContent()
    {
        try
        {
            buffer.flush();
            return buffer.toByteArray();
        } 
        catch(IOException exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
    
    public void clearContent()
    {
        buffer = new ByteArrayOutputStream();
    }
}
