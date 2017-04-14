/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
