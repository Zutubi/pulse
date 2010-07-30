package com.zutubi.pulse.master.jetty;

import com.zutubi.pulse.master.security.AnonymousActor;
import com.zutubi.pulse.master.security.HttpInvocation;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.Actor;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.jetty.servlet.ServletHttpRequest;
import org.mortbay.jetty.servlet.ServletHttpResponse;
import org.springframework.security.ui.AuthenticationEntryPoint;
import org.springframework.security.AuthenticationException;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * An implementation of the {@link org.mortbay.http.HttpHandler} interface
 * that delegates a security check to the acess manager.  If the request is
 * denied and the user is anonymous, then a basic authentication  response
 * will be sent out.  If the user is authenticated but denied, then the
 * resource will be forbidden.
 */
public class SecurityHandler extends AbstractHttpHandler
{
    private AccessManager accessManager;

    private AuthenticationEntryPoint basicEntryPoint;

    public void handle(final String pathInContext, final String pathParams, final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException
    {
        HttpInvocation invocation = new HttpInvocation(httpRequest, httpResponse, pathInContext, accessManager.getActor());

        if (!accessManager.hasPermission(invocation.getMethod(), invocation))
        {
            if (isAnonymousActor(accessManager.getActor()))
            {
                // the user is anonymous and they obviously do not have the permissions to access
                // this resource, so send them a challenge.  This will trigger the basic authentication.

                ServletHttpRequest request = (ServletHttpRequest) httpRequest.getWrapper();
                ServletHttpResponse response = (ServletHttpResponse) httpResponse.getWrapper();
                try
                {
                    // commence the basic authentication response.
                    basicEntryPoint.commence(request, response, new AuthenticationException("Authentication Required"){});
                }
                catch (ServletException e)
                {
                    throw new IOException(e.getMessage());
                }
                httpRequest.setHandled(true);
            }
            else
            {
                // The user has been authenticated but still does not have the permissions to access
                // this resource.  Send them packing.
                httpResponse.sendError(HttpResponse.__403_Forbidden);
                httpRequest.setHandled(true);
            }
        }
        // The user has permissions to access this resource.
    }

    private boolean isAnonymousActor(Actor actor)
    {
        return actor instanceof AnonymousActor;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setBasicEntryPoint(AuthenticationEntryPoint basicEntryPoint)
    {
        this.basicEntryPoint = basicEntryPoint;
    }
}
