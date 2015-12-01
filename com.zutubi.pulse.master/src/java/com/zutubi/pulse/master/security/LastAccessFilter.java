package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.tove.security.Actor;

import javax.servlet.*;
import java.io.IOException;

/**
 * This filter works with the {@link LastAccessManager} to track last access
 * times for the web ui.
 */
public class LastAccessFilter implements Filter
{
    private static final String BEAN_LAST_ACCESS_MANAGER = "lastAccessManager";
    
    private LastAccessManager lastAccessManager;

    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        Actor actor = SecurityUtils.getLoggedInUser();
        if (actor != null && actor instanceof Principle)
        {
            LastAccessManager lam = getLastAccessManager();
            if (lam != null)
            {
                Principle principle = (Principle) actor;
                lastAccessManager.recordAccess(principle.getId());
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
        if (lastAccessManager == null && SpringComponentContext.containsBean(BEAN_LAST_ACCESS_MANAGER))
        {
            lastAccessManager = SpringComponentContext.getOptionalBean(BEAN_LAST_ACCESS_MANAGER);
        }
        return lastAccessManager;
    }

    public void setLastAccessManager(LastAccessManager lastAccessManager)
    {
        this.lastAccessManager = lastAccessManager;
    }
}