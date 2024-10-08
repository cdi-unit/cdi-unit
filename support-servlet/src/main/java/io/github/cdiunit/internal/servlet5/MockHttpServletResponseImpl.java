/*
 * Copyright 2011 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.internal.servlet5;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import io.github.cdiunit.internal.servlet.common.ExceptionUtils;
import io.github.cdiunit.internal.servlet.common.WebConstants;

/**
 * Shamlessly ripped from mockrunner.
 *
 */
public class MockHttpServletResponseImpl implements HttpServletResponse {
    private PrintWriter writer;
    private MockServletOutputStream outputStream;
    private Map headers;
    private Locale locale;
    private String characterEncoding;
    private int bufferSize;
    private boolean wasErrorSent;
    private boolean wasRedirectSent;
    private int errorCode;
    private int statusCode;
    private List cookies;
    private long contentLength;

    public MockHttpServletResponseImpl() {
        resetAll();
    }

    /**
     * Resets the state of this object to the default values
     */
    public void resetAll() {
        headers = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        characterEncoding = "ISO-8859-1";
        bufferSize = 8192;
        wasErrorSent = false;
        wasRedirectSent = false;
        errorCode = HttpServletResponse.SC_OK;
        statusCode = HttpServletResponse.SC_OK;
        cookies = new ArrayList();
        outputStream = new MockServletOutputStream(characterEncoding);
        contentLength = -1;
        try {
            writer = new PrintWriter(new OutputStreamWriter(outputStream,
                    characterEncoding), true);
        } catch (UnsupportedEncodingException exc) {
            throw ExceptionUtils.asRuntimeException(exc);
        }
    }

    public String encodeURL(String url) {
        return url;
    }

    public String encodeRedirectUrl(String url) {
        return url;
    }

    public String encodeRedirectURL(String url) {
        return url;
    }

    public String encodeUrl(String url) {
        return url;
    }

    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    public String getOutputStreamContent() {
        return outputStream.getContent();
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public void addDateHeader(String key, long date) {
        addHeader(key, getDateString(date));
    }

    public void addHeader(String key, String value) {
        List valueList = (List) headers.get(key);
        if (null == valueList) {
            valueList = new ArrayList();
            headers.put(key, valueList);
        }
        valueList.add(value);
    }

    public void addIntHeader(String key, int value) {
        String stringValue = Integer.toString(value);
        addHeader(key, stringValue);
    }

    public boolean containsHeader(String key) {
        return headers.containsKey(key);
    }

    public void sendError(int code, String message) throws IOException {
        errorCode = code;
        wasErrorSent = true;
    }

    public void sendError(int code) throws IOException {
        errorCode = code;
        wasErrorSent = true;
    }

    public void sendRedirect(String location) throws IOException {
        setHeader("Location", location);
        wasRedirectSent = true;
    }

    public void setDateHeader(String key, long date) {
        setHeader(key, getDateString(date));
    }

    public void setHeader(String key, String value) {
        List valueList = new ArrayList();
        headers.put(key, valueList);
        valueList.add(value);
    }

    public void setIntHeader(String key, int value) {
        String stringValue = Integer.toString(value);
        setHeader(key, stringValue);
    }

    public void setStatus(int code, String message) {
        statusCode = code;
    }

    public void setStatus(int code) {
        statusCode = code;
    }

    public void flushBuffer() throws IOException {
        writer.flush();
        outputStream.flush();
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String encoding) {
        characterEncoding = encoding;
        outputStream.setEncoding(encoding);
        try {
            writer = new PrintWriter(new OutputStreamWriter(outputStream,
                    characterEncoding), true);
        } catch (UnsupportedEncodingException exc) {
            throw ExceptionUtils.asRuntimeException(exc);
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
        errorCode = HttpServletResponse.SC_OK;
        statusCode = HttpServletResponse.SC_OK;
        clearHeaders();
        resetBuffer();
    }

    public void resetBuffer() {
        outputStream.clearContent();
    }

    public void clearHeaders() {
        headers.clear();
    }

    public void setBufferSize(int size) {
        bufferSize = size;
    }

    public void setContentLength(int length) {
        setIntHeader("Content-Length", length);
    }

    public String getContentType() {
        return getHeader("Content-Type");
    }

    public void setContentType(String type) {
        setHeader("Content-Type", type);
    }

    public Collection getHeaderNames() {
        return headers.keySet();
    }

    public Collection getHeaders(String name) {
        List headerList = (List) headers.get(name);
        if (null == headerList) {
            return null;
        }
        return headerList;
    }

    public List getHeaderList(String key) {
        return (List) headers.get(key);
    }

    public String getHeader(String key) {
        List list = getHeaderList(key);
        if (null == list || list.isEmpty()) {
            return null;
        }
        return (String) list.get(0);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getStatus() {
        return getStatusCode();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public List getCookies() {
        return cookies;
    }

    public boolean wasErrorSent() {
        return wasErrorSent;
    }

    public boolean wasRedirectSent() {
        return wasRedirectSent;
    }

    private String getDateString(long date) {
        Date dateValue = new Date(date);
        SimpleDateFormat dateFormat = new SimpleDateFormat(WebConstants.DATE_FORMAT_HEADER, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(dateValue);
    }

    public void setContentLengthLong(long len) {
        contentLength = len;
    }
}
