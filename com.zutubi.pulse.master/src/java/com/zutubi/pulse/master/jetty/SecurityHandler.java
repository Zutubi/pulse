package com.zutubi.pulse.master.jetty;

import com.zutubi.pulse.master.security.AnonymousActor;
import com.zutubi.pulse.master.security.HttpInvocation;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.Actor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An implementation of the {@link org.eclipse.jetty.server.handler.HandlerWrapper} interface that
 * delegates a security check to the access manager.  If the request is denied and the user is
 * anonymous, then a basic authentication response will be sent out.  If the user is authenticated
 * but denied, then the resource will be forbidden.
 */
public class SecurityHandler extends HandlerWrapper
{
    private AccessManager accessManager;

    private AuthenticationEntryPoint basicEntryPoint;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        HttpInvocation invocation = new HttpInvocation(request, accessManager.getActor());

        if (!accessManager.hasPermission(invocation.getMethod(), invocation))
        {
            if (isAnonymousActor(accessManager.getActor()))
            {
                // the user is anonymous and they obviously do not have the permissions to access
                // this resource, so send them a challenge.  This will trigger the basic authentication.
                try
                {
                    // commence the basic authentication response.
                    basicEntryPoint.commence(request, response, new AuthenticationException("Authentication Required"){});
                }
                catch (ServletException e)
                {
                    throw new IOException(e.getMessage());
                }
                baseRequest.setHandled(true);
                return;
            }
            else
            {
                // The user has been authenticated but still does not have the permissions to access
                // this resource.  Send them packing.
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                baseRequest.setHandled(true);
                return;
            }
        }

        getHandler().handle(target, baseRequest, request, response);
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
