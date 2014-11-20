package com.zutubi.pulse.master.jetty;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * A HandlerWrapper that applies {@link javax.servlet.Filter}s around the nested handler.
 */
public class FilteringHandler extends HandlerWrapper
{
    private List<Filter> filters = new LinkedList<Filter>();

    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
    {
        try
        {
            FilterChain chain = new FilterChain()
            {
                private int filterCount = 0;

                public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException
                {
                    if (filterCount < filters.size())
                    {
                        filterCount++;
                        Filter f = filters.get(filterCount - 1);
                        f.doFilter(servletRequest, servletResponse, this);
                    }
                    else
                    {
                        // all of the filters have been applied, now we call the delegate.
                        getHandler().handle(target, baseRequest, request, response);
                    }
                }
            };
            chain.doFilter(request, response);
        }
        catch (ServletException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    public void setFilters(List<Filter> filters)
    {
        this.filters = filters;
    }

    public void addFilter(Filter filter)
    {
        filters.add(filter);
    }
}
