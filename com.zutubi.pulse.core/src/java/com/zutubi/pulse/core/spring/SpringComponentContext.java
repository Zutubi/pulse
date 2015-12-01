package com.zutubi.pulse.core.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * The component context is the central storage location for the systems ApplicatonContext
 * object.
 *
 * The main purpose of this is to make the context available to those components for which
 * springs auto-wiring is not available.
 */
public class SpringComponentContext
{
    private static ConfigurableApplicationContext context = null;

    public static ApplicationContext getContext()
    {
        return context;
    }

    public static void addFileContextDefinitions(String... definitions)
    {
        if (definitions != null && definitions.length > 0)
        {
            FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(definitions, false, context);
            publishContext(ctx);
        }
    }

    public static void addClassPathContextDefinitions(String... definitions)
    {
        if (definitions != null && definitions.length > 0)
        {
            ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(definitions, false, context);
            publishContext(ctx);
        }
    }

    /**
     * Pop the current context off the context stack.
     */
    public static void pop()
    {
        ConfigurableApplicationContext top = context;
        context = (ConfigurableApplicationContext) context.getParent();
        
        // not sure if we need to separate the context from the tree.
        top.setParent(null);
        top.close();
    }

    private static void publishContext(ConfigurableApplicationContext newContext)
    {
        newContext.refresh();
        context = newContext;
    }

    public static boolean containsBean(String name)
    {
        return getContext() != null && getContext().containsBean(name);
    }

    @SuppressWarnings("unchecked")
    public static <U> U getBean(String name)
    {
        U bean = null;
        if (getContext() != null)
        {
            bean = (U) getContext().getBean(name);
        }

        if (bean == null)
        {
            throw new IllegalArgumentException("Request for unknown bean '" + name + "'");
        }

        return bean;
    }

    @SuppressWarnings("unchecked")
    public static <U> U getOptionalBean(String name)
    {
        if (getContext() != null)
        {
            return (U) getContext().getBean(name);
        }
        return null;
    }


    public static void autowire(Object bean)
    {
        if (getContext() == null)
        {
            // there is no context to use for the autowiring, so no work
            // can be done.
            return;
        }
        SpringAutowireSupport support = new SpringAutowireSupport();
        support.setApplicationContext(context);
        support.autoWireBean(bean);
    }

    public static void closeAll()
    {
        if (getContext() != null)
        {
            ConfigurableApplicationContext ctx = context;
            while (ctx != null)
            {
                ctx.close();
                ctx = (ConfigurableApplicationContext) ctx.getParent();
            }
            context = null;
        }
    }
}
