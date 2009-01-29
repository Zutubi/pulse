package com.zutubi.pulse.servercore.jetty;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

import java.io.IOException;

/**
 * An implementation of the {@link org.mortbay.http.HttpHandler} interface
 * that delegates a security check to a {@link PrivilegeEvaluator}.  If the request
 * is denied, the request is marked as handled and the forbidden error is sent via
 * the response.
 */
public class SecurityHandler extends AbstractHttpHandler
{
    private PrivilegeEvaluator privilegeEvaluator;

    public void handle(final String pathInContext, final String pathParams, final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException
    {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        HttpInvocation invocation = new HttpInvocation(httpRequest, httpResponse);

        if (!privilegeEvaluator.isAllowed(invocation, authentication))
        {
            httpResponse.sendError(HttpResponse.__403_Forbidden);
            httpRequest.setHandled(true);
        }
    }

    public void setPrivilegeEvaluator(PrivilegeEvaluator privilegeEvaluator)
    {
        this.privilegeEvaluator = privilegeEvaluator;
    }
}
