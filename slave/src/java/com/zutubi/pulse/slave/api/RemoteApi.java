package com.zutubi.pulse.slave.api;

import com.zutubi.pulse.ShutdownManager;
import com.zutubi.pulse.api.AdminTokenManager;
import com.zutubi.pulse.api.AuthenticationException;
import com.zutubi.pulse.bootstrap.ComponentContext;

/**
 */
public class RemoteApi
{
    private AdminTokenManager tokenManager;
    private ShutdownManager shutdownManager;

    public RemoteApi()
    {
        ComponentContext.autowire(this);
    }

    public boolean shutdown(String token, boolean force, boolean exitJvm) throws AuthenticationException
    {
        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        if(tokenManager.checkAdminToken(token))
        {
            shutdownManager.delayedShutdown(force, exitJvm);
        }
        else
        {
            throw new AuthenticationException("Invalid token");
        }

        return true;
    }

    public void setTokenManager(AdminTokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }
}
