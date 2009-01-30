package com.zutubi.pulse.servercore.jetty;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

import java.io.IOException;

import com.zutubi.tove.security.AccessManager;

/**
 * An implementation of the {@link org.mortbay.http.HttpHandler} interface
 * that delegates a security check to a {@link PrivilegeEvaluator}.  If the request
 * is denied, the request is marked as handled and the forbidden error is sent via
 * the response.
 */
public class SecurityHandler extends AbstractHttpHandler
{
    private AccessManager accessManager;

    public void handle(final String pathInContext, final String pathParams, final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException
    {
        HttpInvocation invocation = new HttpInvocation(httpRequest, httpResponse, pathInContext);

        if (!accessManager.hasPermission(invocation.getMethod(), invocation))
        {
            httpResponse.sendError(HttpResponse.__403_Forbidden);
            httpRequest.setHandled(true);
        }
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }
}
