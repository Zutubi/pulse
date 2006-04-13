/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class RedirectServletFilter implements Filter
{
    private static final String DESTINATION_PARAM_KEY = "destination";

    private String destination;

    public void destroy()
    {

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        if (destination != null)
        {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.sendRedirect(destination);
        }
    }

    public void init(FilterConfig config) throws ServletException
    {
        destination = config.getInitParameter(DESTINATION_PARAM_KEY);
        if (destination == null)
        {
            // default...
        }
    }
}
