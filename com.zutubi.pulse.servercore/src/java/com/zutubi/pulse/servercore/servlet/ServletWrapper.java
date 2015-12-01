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
