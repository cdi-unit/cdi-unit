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
