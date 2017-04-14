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
 *  This servlet implementation is used to support autowiring of the specified delegate.
 */
public class ServletWrapper implements Servlet
{
    /**
     * The name of the init parameter for specifying the delegate servlet classname.
     */
    private static final String DELEGATE_CLASS_NAME = "delegateClassName";

    /**
     * The wrapped delegate.
     */
    private Servlet delegate;

    /**
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig servletConfig) throws ServletException
    {
        delegate = createDelegate(servletConfig);
        delegate.init(servletConfig);
    }

    /**
     * @see javax.servlet.Servlet#getServletConfig()
     */
    public ServletConfig getServletConfig()
    {
        return getDelegate().getServletConfig();
    }

    /**
     * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException
    {
        getDelegate().service(servletRequest, servletResponse);
    }

    /**
     * @see javax.servlet.Servlet#getServletInfo()
     */
    public String getServletInfo()
    {
        return getDelegate().getServletInfo();
    }

    /**
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy()
    {
        getDelegate().destroy();
    }

    /**
     * Handle the creation of the delegate based on the servlet configuration.
     *
     * @param servletConfig
     *
     * @return
     *
     * @throws ServletException
     */
    protected Servlet createDelegate(ServletConfig servletConfig) throws ServletException
    {
        String className = servletConfig.getInitParameter(DELEGATE_CLASS_NAME);
        if (!StringUtils.stringSet(className))
        {
            throw new ServletException("Required init parameter " + DELEGATE_CLASS_NAME + " is missing.");
        }

        ObjectFactory objectFactory = SpringComponentContext.getBean("objectFactory");
        return objectFactory.buildBean(className, Servlet.class);
    }

    protected Servlet getDelegate()
    {
        return delegate;
    }
}
