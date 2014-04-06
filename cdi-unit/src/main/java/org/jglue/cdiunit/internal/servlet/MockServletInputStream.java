package org.jglue.cdiunit.internal.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletInputStream;

/**
 * Mock implementation of <code>ServletInputStream</code>.
 */
public class MockServletInputStream extends ServletInputStream
{
    private ByteArrayInputStream stream;
    
    public MockServletInputStream(byte[] data)
    {
        stream = new ByteArrayInputStream(data);
    }
        
    public int read() throws IOException
    {
        return stream.read();
    }
}
