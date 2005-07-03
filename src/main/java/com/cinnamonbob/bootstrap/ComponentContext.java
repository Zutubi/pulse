package com.cinnamonbob.bootstrap;

import org.springframework.context.ApplicationContext;
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
    private static ApplicationContext context = null;
    
    public static ApplicationContext getContext()
    {
        return context;
    }

    public static void addFileContextDefinitions(String[] definitions)
    {
        if (definitions != null && definitions.length > 0)
        {
            context = new FileSystemXmlApplicationContext(definitions, context);
        }
    }

    public static void addClassPathContextDefinitions(String[] definitions)
    {
        if (definitions != null && definitions.length > 0)
        {
            context = new ClassPathXmlApplicationContext(definitions, context);
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
}
