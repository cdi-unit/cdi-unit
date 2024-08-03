/*
 * Copyright 2014 the original author or authors.
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
package io.github.cdiunit.internal.jaxrs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.*;

import org.jboss.resteasy.core.request.ServerDrivenNegotiation;
import org.jboss.resteasy.plugins.server.servlet.ServletUtil;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.util.DateUtil;
import org.jboss.resteasy.util.Encode;
import org.jboss.resteasy.util.HttpHeaderNames;

/**
 * Treaked from resteasy RequestImpl
 */
public class RequestImpl implements Request {
    private final HttpHeaders headers;
    private String varyHeader;
    private final String httpMethod;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public RequestImpl(HttpServletRequest request, HttpServletResponse response) {
        this.headers = ServletUtil.extractHttpHeaders(request);
        this.httpMethod = request.getMethod().toUpperCase();
        this.request = request;
        this.response = response;
    }

    public String getMethod() {
        return httpMethod;
    }

    public MultivaluedMap<String, String> getFormParameters() {
        MultivaluedMapImpl<String, String> params = new MultivaluedMapImpl<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            for (String value : entry.getValue()) {
                params.add(entry.getKey(), value);
            }
        }
        return Encode.decode(params);
    }

    public Variant selectVariant(List<Variant> variants) throws IllegalArgumentException {
        if (variants == null || variants.isEmpty()) {
            throw new IllegalArgumentException("Variant list must not be zero");
        }

        ServerDrivenNegotiation negotiation = new ServerDrivenNegotiation();
        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        negotiation.setAcceptHeaders(requestHeaders.get(HttpHeaderNames.ACCEPT));
        negotiation.setAcceptCharsetHeaders(requestHeaders.get(HttpHeaderNames.ACCEPT_CHARSET));
        negotiation.setAcceptEncodingHeaders(requestHeaders.get(HttpHeaderNames.ACCEPT_ENCODING));
        negotiation.setAcceptLanguageHeaders(requestHeaders.get(HttpHeaderNames.ACCEPT_LANGUAGE));

        varyHeader = createVaryHeader(variants);
        response.addHeader(HttpHeaderNames.VARY, varyHeader);
        return negotiation.getBestMatch(variants);
    }

    public static String createVaryHeader(List<Variant> variants) {
        boolean accept = false;
        boolean acceptLanguage = false;
        boolean acceptEncoding = false;

        for (Variant variant : variants) {
            if (variant.getMediaType() != null) {
                accept = true;
            }
            if (variant.getLanguage() != null) {
                acceptLanguage = true;
            }
            if (variant.getEncoding() != null) {
                acceptEncoding = true;
            }
        }

        String vary = null;
        if (accept) {
            vary = HttpHeaderNames.ACCEPT;
        }
        if (acceptLanguage) {
            if (vary == null) {
                vary = HttpHeaderNames.ACCEPT_LANGUAGE;
            } else {
                vary += ", " + HttpHeaderNames.ACCEPT_LANGUAGE;
            }
        }
        if (acceptEncoding) {
            if (vary == null) {
                vary = HttpHeaderNames.ACCEPT_ENCODING;
            } else {
                vary += ", " + HttpHeaderNames.ACCEPT_ENCODING;
            }
        }
        return vary;
    }

    public List<EntityTag> convertEtag(List<String> tags) {
        ArrayList<EntityTag> result = new ArrayList<>();
        for (String tag : tags) {
            String[] split = tag.split(",");
            for (String etag : split) {
                result.add(EntityTag.valueOf(etag.trim()));
            }
        }
        return result;
    }

    public Response.ResponseBuilder ifMatch(List<EntityTag> ifMatch, EntityTag eTag) {
        boolean match = false;
        for (EntityTag tag : ifMatch) {
            if (tag.equals(eTag) || "*".equals(tag.getValue())) {
                match = true;
                break;
            }
        }
        if (match) {
            return null;
        }
        return Response.status(Response.Status.PRECONDITION_FAILED).tag(eTag);

    }

    public Response.ResponseBuilder ifNoneMatch(List<EntityTag> ifMatch, EntityTag eTag) {
        boolean match = false;
        for (EntityTag tag : ifMatch) {
            if (tag.equals(eTag) || "*".equals(tag.getValue())) {
                match = true;
                break;
            }
        }
        if (match) {
            if ("GET".equals(httpMethod) || "HEAD".equals(httpMethod)) {
                return Response.notModified(eTag);
            }

            return Response.status(Response.Status.PRECONDITION_FAILED).tag(eTag);
        }
        return null;
    }

    public Response.ResponseBuilder evaluatePreconditions(EntityTag eTag) {
        if (eTag == null) {
            throw new IllegalArgumentException("eTag param null");
        }
        Response.ResponseBuilder builder = null;
        List<String> ifMatch = headers.getRequestHeaders().get(HttpHeaderNames.IF_MATCH);
        if (ifMatch != null && !ifMatch.isEmpty()) {
            builder = ifMatch(convertEtag(ifMatch), eTag);
        }
        if (builder == null) {
            List<String> ifNoneMatch = headers.getRequestHeaders().get(HttpHeaderNames.IF_NONE_MATCH);
            if (ifNoneMatch != null && !ifNoneMatch.isEmpty()) {
                builder = ifNoneMatch(convertEtag(ifNoneMatch), eTag);
            }
        }
        if (builder != null) {
            builder.tag(eTag);
        }
        if (builder != null && varyHeader != null) {
            builder.header(HttpHeaderNames.VARY, varyHeader);
        }
        return builder;
    }

    public Response.ResponseBuilder ifModifiedSince(String strDate, Date lastModified) {
        Date date = DateUtil.parseDate(strDate);

        if (date.getTime() >= lastModified.getTime()) {
            return Response.notModified();
        }
        return null;

    }

    public Response.ResponseBuilder ifUnmodifiedSince(String strDate, Date lastModified) {
        Date date = DateUtil.parseDate(strDate);

        if (date.getTime() >= lastModified.getTime()) {
            return null;
        }
        return Response.status(Response.Status.PRECONDITION_FAILED).lastModified(lastModified);

    }

    public Response.ResponseBuilder evaluatePreconditions(Date lastModified) {
        if (lastModified == null) {
            throw new IllegalArgumentException("lastModified param null");
        }
        Response.ResponseBuilder builder = null;
        String ifModifiedSince = headers.getRequestHeaders().getFirst(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null) {
            builder = ifModifiedSince(ifModifiedSince, lastModified);
        }
        if (builder == null) {
            // System.out.println("ifModified returned null");
            String ifUnmodifiedSince = headers.getRequestHeaders().getFirst(HttpHeaderNames.IF_UNMODIFIED_SINCE);
            if (ifUnmodifiedSince != null) {
                builder = ifUnmodifiedSince(ifUnmodifiedSince, lastModified);
            }
        }
        if (builder != null && varyHeader != null) {
            builder.header(HttpHeaderNames.VARY, varyHeader);
        }

        return builder;
    }

    public Response.ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
        if (lastModified == null) {
            throw new IllegalArgumentException("lastModified param null");
        }
        if (eTag == null) {
            throw new IllegalArgumentException("eTag param null");
        }
        Response.ResponseBuilder rtn = null;
        Response.ResponseBuilder lastModifiedBuilder = evaluatePreconditions(lastModified);
        Response.ResponseBuilder etagBuilder = evaluatePreconditions(eTag);
        if (lastModifiedBuilder == null && etagBuilder == null) {
            rtn = null;
        } else if (lastModifiedBuilder != null && etagBuilder == null) {
            rtn = lastModifiedBuilder;
        } else if (lastModifiedBuilder == null && etagBuilder != null) {
            rtn = etagBuilder;
        } else {
            rtn = lastModifiedBuilder;
            rtn.tag(eTag);
        }
        if (rtn != null && varyHeader != null) {
            rtn.header(HttpHeaderNames.VARY, varyHeader);
        }
        return rtn;
    }

    public Response.ResponseBuilder evaluatePreconditions() {
        List<String> ifMatch = headers.getRequestHeaders().get(HttpHeaderNames.IF_MATCH);
        if (ifMatch == null || ifMatch.isEmpty()) {
            return null;
        }

        return Response.status(Response.Status.PRECONDITION_FAILED);
    }

}
