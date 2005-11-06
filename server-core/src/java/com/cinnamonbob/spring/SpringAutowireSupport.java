package com.cinnamonbob.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.logging.Logger;

/**
 * <class-comment/>
 */
public class SpringAutowireSupport implements ApplicationContextAware
{
    private static final Logger LOG = Logger.getLogger(SpringAutowireSupport.class.getName());

    // Autowire support taken from com.opensymphony.xwork.spring.SpringObjectFactory
    private AutowireCapableBeanFactory autoWiringFactory;

    private int autowireStrategy = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    private ApplicationContext context;

    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        this.context = context;
        findAutoWiringBeanFactory(this.context);
    }

    /**
     * If the given context is assignable to AutowireCapbleBeanFactory or
     * contains a parent or a factory that is, then set the autoWiringFactory
     * appropriately.
     *
     * @param context
     */
    private void findAutoWiringBeanFactory(ApplicationContext context)
    {
        if (context instanceof AutowireCapableBeanFactory)
        {
            // Check the context
            autoWiringFactory = (AutowireCapableBeanFactory) context;
        }
        else if (context instanceof ConfigurableApplicationContext)
        {
            // Try and grab the beanFactory
            autoWiringFactory = ((ConfigurableApplicationContext) context)
                    .getBeanFactory();
        }
        else if (context.getParent() != null)
        {
            // And if all else fails, try again with the parent context
            findAutoWiringBeanFactory(context.getParent());
        }
    }

    /**
     * @param bean
     */
    public Object autoWireBean(Object bean)
    {
        if (autoWiringFactory != null)
            autoWiringFactory.autowireBeanProperties(bean, autowireStrategy, false);

        if (bean instanceof ApplicationContextAware)
            ((ApplicationContextAware) bean).setApplicationContext(context);

        return bean;
    }

    /**
     * Sets the autowiring strategy
     *
     * @param autowireStrategy
     */
    public void setAutowireStrategy(int autowireStrategy)
    {
        switch (autowireStrategy)
        {
            case AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT:
                LOG.info("Setting autowire strategy to autodetect");
                this.autowireStrategy = autowireStrategy;
                break;
            case AutowireCapableBeanFactory.AUTOWIRE_BY_NAME:
                LOG.info("Setting autowire strategy to name");
                this.autowireStrategy = autowireStrategy;
                break;
            case AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE:
                LOG.info("Setting autowire strategy to type");
                this.autowireStrategy = autowireStrategy;
                break;
            case AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR:
                LOG.info("Setting autowire strategy to constructor");
                this.autowireStrategy = autowireStrategy;
                break;
            default:
                throw new IllegalStateException("Invalid autowire type set");
        }
    }

    public int getAutowireStrategy()
    {
        return autowireStrategy;
    }
}
