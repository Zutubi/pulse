package com.zutubi.pulse.servercore.jetty;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 * A value holder that contains all the available details for a http request/response - invocation.
 */
public class HttpInvocation
{
    private final HttpRequest httpRequest;
    
    private final HttpResponse httpResponse;

    private String pathInContext;

    public HttpInvocation(HttpRequest httpRequest, HttpResponse httpResponse, String pathInContext)
    {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.pathInContext = pathInContext;
    }

    public String getMethod()
    {
        return httpRequest.getMethod();
    }

    public String getPath()
    {
        return httpRequest.getPath();
    }

    public HttpRequest getHttpRequest()
    {
        return httpRequest;
    }

    public HttpResponse getHttpResponse()
    {
        return httpResponse;
    }

    public String getPathInContext()
    {
        return pathInContext;
    }
}
