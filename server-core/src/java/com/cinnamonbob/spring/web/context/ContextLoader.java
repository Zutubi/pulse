package com.cinnamonbob.spring.web.context;

import com.cinnamonbob.bootstrap.ComponentContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

/**
 * <class-comment/>
 */
public class ContextLoader extends org.springframework.web.context.ContextLoader
{
    protected WebApplicationContext createWebApplicationContext(ServletContext servletContext, ApplicationContext parent) throws BeansException
    {
        BenignWebApplicationContext context = new BenignWebApplicationContext();
        context.setParent(ComponentContext.getContext());
        context.setServletContext(servletContext);
        context.refresh();
        return context;
    }
}
