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
            context.setDelegate(new FileSystemXmlApplicationContext(definitions, false, context.getDelegate()));
            ((ConfigurableApplicationContext)context.getDelegate()).refresh();
        }
    }

    public static void addClassPathContextDefinitions(String[] definitions)
    {
        if (definitions != null && definitions.length > 0)
        {
            context.setDelegate(new ClassPathXmlApplicationContext(definitions, false, context.getDelegate()));
            ((ConfigurableApplicationContext)context.getDelegate()).refresh();
        }
    }

    public static Object getBean(String name)
    {
        if (getContext() != null)
        {
            return getContext().getBean(name);
        }
        return null;
    }

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
