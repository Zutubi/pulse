/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.spring;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.ObjectFactory;
import com.opensymphony.util.TextUtils;

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
            throw new ServletException("Error creating filter delegate.", e);
        }
    }

    protected Filter getDelegate()
    {
        return delegate;
    }
}
