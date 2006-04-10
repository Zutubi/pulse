package com.cinnamonbob.spring;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core.ObjectFactory;
import com.opensymphony.util.TextUtils;

import javax.servlet.*;
import java.io.IOException;

/**
 *  This servlet implementation is used to support autowiring of the specified delegate.
 *
 * @author Daniel Ostermeier
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
        if (!TextUtils.stringSet(className))
        {
            throw new ServletException("Required init parameter " + DELEGATE_CLASS_NAME + " is missing.");
        }

        try
        {
            ObjectFactory objectFactory = (ObjectFactory) ComponentContext.getBean("objectFactory");
            return objectFactory.buildBean(className);
        }
        catch (Exception e)
        {
            throw new ServletException("Error creating servlet delegate.", e);
        }
    }

    protected Servlet getDelegate()
    {
        return delegate;
    }
}
