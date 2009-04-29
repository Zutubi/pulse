package com.zutubi.pulse.master.security;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import com.zutubi.tove.security.Actor;

/**
 * A value holder that contains all the available details for a http request/response - invocation.
 */
public class HttpInvocation
{
    private final HttpRequest httpRequest;

    private final HttpResponse httpResponse;

    private String pathInContext;
    
    private Actor actor;

    public HttpInvocation(HttpRequest httpRequest, HttpResponse httpResponse, String pathInContext, Actor actor)
    {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.pathInContext = pathInContext;
        this.actor = actor;
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

    public Actor getActor()
    {
        return actor;
    }
}
