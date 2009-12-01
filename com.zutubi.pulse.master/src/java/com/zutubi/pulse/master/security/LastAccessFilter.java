package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.tove.security.Actor;

import javax.servlet.*;
import java.io.IOException;

/**
 * Overrides the default Acegi implementation to force eager session
 * creation.  Configuring this by normal means does not work due to a
 * combination of needing this filter around the sitemesh one and the fact
 * that we dynamically deploy the rest of the security filters at run time
 * (which makes them within sitemesh, meaning the security context is cleaned
 * out of the session and sitemesh does not see the logged in user).
 */
public class LastAccessFilter implements Filter
{
    private LastAccessManager lastAccessManager;

    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        Actor actor = AcegiUtils.getLoggedInUser();
        if (actor != null && actor instanceof AcegiUser)
        {
            LastAccessManager lam = getLastAccessManager();
            if (lam != null)
            {
                AcegiUser acegiUser = (AcegiUser) actor;
                lastAccessManager.recordAccess(acegiUser.getId());
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy()
    {
    }

    public LastAccessManager getLastAccessManager()
    {
        // Sadly this class defies wiring as it is initialised too early.
        if (lastAccessManager == null)
        {
            lastAccessManager = SpringComponentContext.getBean("lastAccessManager");
        }
        return lastAccessManager;
    }

    public void setLastAccessManager(LastAccessManager lastAccessManager)
    {
        this.lastAccessManager = lastAccessManager;
    }
}