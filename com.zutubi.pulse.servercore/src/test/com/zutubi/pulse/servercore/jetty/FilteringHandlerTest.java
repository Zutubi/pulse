package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

import javax.servlet.*;
import java.io.IOException;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class FilteringHandlerTest extends PulseTestCase
{
    private HttpResponse response;
    private HttpRequest request;
    private String context;
    private String params;

    protected void setUp() throws Exception
    {
        super.setUp();

        response = mock(HttpResponse.class);
        request = mock(HttpRequest.class);

        context = "context";
        params = "params";
    }

    public void testNoFilters() throws IOException
    {
        HttpHandler delegate = mock(HttpHandler.class);

        handle(delegate);

        verify(delegate, times(1)).handle(eq(context), eq(params), same(request), same(response));
    }

    public void testOneFilter() throws IOException, ServletException
    {
        HttpHandler delegate = mock(HttpHandler.class);
        CountingFilter filter = new CountingFilter(new ChainingFilter());

        handle(delegate, filter);

        verify(delegate, times(1)).handle(eq(context), eq(params), same(request), same(response));
        assertEquals(1, filter.times());
    }

    public void testMultileFilters() throws IOException
    {
        HttpHandler delegate = mock(HttpHandler.class);
        CountingFilter filter1 = new CountingFilter(new ChainingFilter());
        CountingFilter filter2 = new CountingFilter(new ChainingFilter());
        CountingFilter filter3 = new CountingFilter(new ChainingFilter());

        handle(delegate, filter1, filter2, filter3);

        verify(delegate, times(1)).handle(eq(context), eq(params), same(request), same(response));
        assertEquals(1, filter1.times());
        assertEquals(1, filter2.times());
        assertEquals(1, filter3.times());
    }

    public void testFilterAllowsBypass() throws IOException
    {
        HttpHandler delegate = mock(HttpHandler.class);
        CountingFilter filter1 = new CountingFilter(new BypassingFilter());
        CountingFilter filter2 = new CountingFilter(new ChainingFilter());

        handle(delegate, filter1, filter2);

        verify(delegate, times(0)).handle(eq(context), eq(params), same(request), same(response));
        assertEquals(1, filter1.times());
        assertEquals(0, filter2.times());
    }

    private void handle(HttpHandler delegate, CountingFilter... filters) throws IOException
    {
        FilteringHandler handler = new FilteringHandler();
        handler.setDelegate(delegate);
        for (Filter filter : filters)
        {
            handler.addFilter(filter);
        }
        handler.handle(context, params, request, response);
    }

    private class CountingFilter implements Filter
    {
        private int times = 0;

        private OnFilter handler;

        protected CountingFilter(OnFilter handler)
        {
            this.handler = handler;
        }

        public void init(FilterConfig config) throws ServletException
        {
        }

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
        {
            times++;
            handler.execute(request, response, chain);
        }

        public void destroy()
        {
        }

        public int times()
        {
            return times;
        }
    }

    private interface OnFilter
    {
        void execute(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException;
    }

    /**
     * A simple filter implementation that does not chain the request.
     */
    private class BypassingFilter implements OnFilter
    {
        public void execute(ServletRequest request, ServletResponse response, FilterChain chain)
        {

        }
    }

    /**
     * A simple filter implementation that chains the request.
     */
    private class ChainingFilter implements OnFilter
    {
        public void execute(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
        {
            chain.doFilter(request, response);
        }
    }
}
