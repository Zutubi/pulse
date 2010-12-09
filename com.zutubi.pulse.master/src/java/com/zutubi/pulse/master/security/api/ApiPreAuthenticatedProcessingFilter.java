package com.zutubi.pulse.master.security.api;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.api.DefaultTokenManager;
import com.zutubi.pulse.servercore.api.AuthenticationException;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;

/**
 * An implementation of the {@link AbstractPreAuthenticatedProcessingFilter} that allows use of the
 * encoded api authentication token {@link com.zutubi.pulse.servercore.api.APIAuthenticationToken}
 * used by the {@link com.zutubi.pulse.master.api.RemoteApi} to be used to access the web ui.
 *
 * This filter extracts the token from the PULSE_API_TOKEN request header.
 */
public class ApiPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter
{
    public static final String REQUEST_HEADER = "PULSE_API_TOKEN";

    /**
     * The token manager used by the remote api to generate valid tokens.  This
     * token manager is also in charge of controlling the expiry of the tokens.
     */
    private DefaultTokenManager tokenManager;

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request)
    {
        String token = request.getHeader(REQUEST_HEADER);
        if (token != null && tokenManager != null)
        {
            try
            {
                return tokenManager.verifyToken(token);
            }
            catch (AuthenticationException e)
            {
                return null;
            }
        }
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request)
    {
        return request.getHeader(REQUEST_HEADER);
    }

    public void setTokenManager(DefaultTokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        // security objects are created before much of the rest of the system
        // so we need to wait for the system to start to finish wiring ourselves
        eventManager.register(new SystemStartedListener()
        {
            @Override
            public void systemStarted()
            {
                SpringComponentContext.autowire(ApiPreAuthenticatedProcessingFilter.this);
            }
        });
    }
}
