package com.zutubi.pulse.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class ForwardServletFilter implements Filter
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
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            request.getRequestDispatcher(destination).forward(servletRequest, servletResponse);
        }
    }

    public void init(FilterConfig config) throws ServletException
    {
        destination = config.getInitParameter(ForwardServletFilter.DESTINATION_PARAM_KEY);
        if (destination == null)
        {
            // default...
        }
    }
}
