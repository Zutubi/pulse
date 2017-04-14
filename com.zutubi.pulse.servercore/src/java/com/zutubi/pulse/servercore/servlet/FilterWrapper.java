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

package com.zutubi.pulse.servercore.servlet;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * This filter implementation is used to support autowiring of the specified delegate.
 *
 *
 * This is useful when the servlet container creating the filter does not support a custom ObjectFactory
 * that can be used to wire the filter on creation.
 *
 * @author Daniel Ostermeier
 */
public class FilterWrapper implements Filter
{
    /**
     * The name of the init parameter for specifying the delegate filter classname.
     */
    private static final String DELEGATE_CLASS_NAME = "delegateClassName";

    /**
     * The delegate filter instance.
     */
    private Filter delegate;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // create the delegate.
        delegate = createDelegate(filterConfig);
        delegate.init(filterConfig);
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        getDelegate().doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
        getDelegate().destroy();
    }

    /**
     * Handle the creation of the delegate based on the filter configuration.
     *
     * @param filterConfig
     *
     * @return
     *
     * @throws ServletException
     */
    protected Filter createDelegate(FilterConfig filterConfig) throws ServletException
    {
        String className = filterConfig.getInitParameter(DELEGATE_CLASS_NAME);
        if (!StringUtils.stringSet(className))
        {
            throw new ServletException("Required init parameter " + DELEGATE_CLASS_NAME + " is missing.");
        }

        ObjectFactory objectFactory = SpringComponentContext.getBean("objectFactory");
        return objectFactory.buildBean(className, Filter.class);
    }

    protected Filter getDelegate()
    {
        return delegate;
    }
}
