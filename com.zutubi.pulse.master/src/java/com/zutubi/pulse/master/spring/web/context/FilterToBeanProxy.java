package com.zutubi.pulse.master.spring.web.context;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import org.springframework.context.ApplicationContext;

import javax.servlet.FilterConfig;

/**
 * <class-comment/>
 */
public class FilterToBeanProxy extends org.springframework.security.util.FilterToBeanProxy
{
    protected ApplicationContext getContext(FilterConfig filterConfig)
    {
        // can not autowire since this object is created by Jetty.
        return SpringComponentContext.getContext();
    }
}
