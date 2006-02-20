package com.cinnamonbob.bootstrap;

import com.cinnamonbob.spring.DelegatingApplicationContext;
import com.cinnamonbob.spring.SpringAutowireSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * The component context is the central storage location for the systems ApplicatonContext 
 * object. We need some way to make the context available to those components for which 
 * springs auto-wiring is not available.
 *
 */
public class ComponentContext
{
    private static final DelegatingApplicationContext context = new DelegatingApplicationContext();

    public static ApplicationContext getContext()
    {
        return context;
    }

    public static void addFileContextDefinitions(String[] definitions)
    {
        if (definitions != null && definitions.length > 0)
        {
            ApplicationContext parent = ComponentContext.context.getDelegate();
            FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(definitions, false, parent);
            setContext(context);
            context.refresh();
        }
    }

    public static void addClassPathContextDefinitions(String[] definitions)
    {
        if (definitions != null && definitions.length > 0)
        {
            ApplicationContext parent = ComponentContext.context.getDelegate();
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(definitions, false, parent);
            setContext(context);
            context.refresh();
        }
    }

    public static void addContext(ConfigurableApplicationContext ctx)
    {
        ctx.setParent(context.getDelegate());
        context.setDelegate(ctx);
    }

    public static void setContext(ConfigurableApplicationContext ctx)
    {
        context.setDelegate(ctx);
    }

    /**
     * Retrieve the named bean from the spring context, or null if it can not be found.
     * @param name
     *
     * @return named object, or null if it can not be located.
     */
    public static Object getBean(String name)
    {
        if (getContext() != null)
        {
            return getContext().getBean(name);
        }
        return null;
    }

    /**
     * Autowrite the specified object using the current context.
     *
     * @param bean
     */
    public static void autowire(Object bean)
    {
        if (getContext() != null)
        {
            SpringAutowireSupport support = new SpringAutowireSupport();
            support.setApplicationContext(context);
            support.autoWireBean(bean);
        }
    }
}
