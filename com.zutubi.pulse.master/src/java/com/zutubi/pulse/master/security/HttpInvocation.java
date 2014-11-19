package com.zutubi.pulse.master.security;

import com.zutubi.tove.security.Actor;

import javax.servlet.http.HttpServletRequest;

/**
 * A value holder that contains all the available details for a http request/response - invocation.
 */
public class HttpInvocation
{
    private final HttpServletRequest httpRequest;
    private Actor actor;

    public HttpInvocation(HttpServletRequest httpRequest, Actor actor)
    {
        this.httpRequest = httpRequest;
        this.actor = actor;
    }

    public String getMethod()
    {
        return httpRequest.getMethod();
    }

    public String getPath()
    {
        return httpRequest.getRequestURI();
    }

    public Actor getActor()
    {
        return actor;
    }
}
