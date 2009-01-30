package com.zutubi.pulse.servercore.jetty;

import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpHandler;
import org.mortbay.jetty.servlet.ServletHttpRequest;
import org.mortbay.jetty.servlet.ServletHttpResponse;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

/**
 * An implementation of a handler that delegates the handling to a delegate
 * handler.  This handler also allows for {@link javax.servlet.Filter}s to be
 * configured around the handler.
 */
public class FilteringHandler extends AbstractHttpHandler
{
    private List<Filter> filters = new LinkedList<Filter>();

    private HttpHandler delegate;

    public void handle(final String pathInContext, final String pathParams, final HttpRequest httpRequest, final HttpResponse httpResponse) throws HttpException, IOException
    {
        ServletHttpRequest request = (ServletHttpRequest) httpRequest.getWrapper();
        ServletHttpResponse response = (ServletHttpResponse) httpResponse.getWrapper();

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
                        delegate.handle(pathInContext, pathParams, httpRequest,  httpResponse);
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

    public void setDelegate(HttpHandler delegate)
    {
        this.delegate = delegate;
    }
}
