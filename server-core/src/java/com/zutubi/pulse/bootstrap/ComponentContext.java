/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.spring.DelegatingApplicationContext;
import com.zutubi.pulse.spring.SpringAutowireSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * The component context is the central storage location for the systems ApplicatonContext
 * object. We need some way to make the context available to those components for which
 * springs auto-wiring is not available.
 */
public class ComponentContext
{
    // Some implementation notes:
    // a) there are times when an IllegalStateException will be generated because ComponentContext.getBean
    //    is called while context.getDelegate().refresh() is running. In this case, resist the urge to
    //    isolate the loading/refresh of the context from making the context available by splitting the
    //    new FileSystemContext and setDelegate calls. It causes other problems with acegi not being able
    //    to initialise itself due to a ComponentContext.getContext() call that looks up a component.

    private static final DelegatingApplicationContext context = new DelegatingApplicationContext();

    public static ApplicationContext getContext()
    {
        return context;
    }

    public static void addFileContextDefinitions(String... definitions)
    {
        if (definitions != null && definitions.length > 0)
        {
            context.setDelegate(new FileSystemXmlApplicationContext(definitions, false, context.getDelegate()));
            ((ConfigurableApplicationContext) context.getDelegate()).refresh();
        }
    }

    public static void addClassPathContextDefinitions(String... definitions)
    {
        if (definitions != null && definitions.length > 0)
        {
            context.setDelegate(new ClassPathXmlApplicationContext(definitions, false, context.getDelegate()));
            ((ConfigurableApplicationContext) context.getDelegate()).refresh();
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
        if (context.getDelegate() == null)
        {
            // there is no context to use for the autowiring, so no work
            // can be done.
            return;
        }
        SpringAutowireSupport support = new SpringAutowireSupport();
        support.setApplicationContext(context);
        support.autoWireBean(bean);
    }

    public static <U> U createBean(Class beanClass) throws Exception
    {
        SpringAutowireSupport support = new SpringAutowireSupport();
        support.setApplicationContext(context);
        return (U) support.createWiredBean(beanClass);
    }

    public static void closeAll()
    {
        context.closeAll();
    }
}
