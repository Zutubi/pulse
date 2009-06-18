package com.zutubi.pulse.master.security;

import org.acegisecurity.context.SecurityContextHolder;

import javax.servlet.*;
import java.io.IOException;

/**
 * A cut down version of the HttpSessionContextIntegrationFilter that retains
 * the functionality to clear the security context holder when the filter chain
 * processing is complete.
 */
public class BasicClearContextFilter implements Filter
{
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // noop.
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        try
        {
            filterChain.doFilter(servletRequest, servletResponse);
        }
        finally
        {
            SecurityContextHolder.clearContext();
        }
    }

    public void destroy()
    {
        // noop.
    }
}
