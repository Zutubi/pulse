package com.zutubi.pulse.core.spring;

import com.zutubi.util.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Autowire support has been adapted from com.opensymphony.xwork.spring.SpringObjectFactory
 */
class SpringAutowireSupport implements ApplicationContextAware
{
    private static final Logger LOG = Logger.getLogger(SpringAutowireSupport.class);

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
     * @param context application context
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
     * Autowire an existing object based on the current application context.
     *
     * @param bean instance
     *
     * @return autowired bean instance.
     */
    public Object autoWireBean(Object bean)
    {
        if (autoWiringFactory != null)
            autoWiringFactory.autowireBeanProperties(bean, autowireStrategy, false);

        if (bean instanceof ApplicationContextAware)
            ((ApplicationContextAware) bean).setApplicationContext(context);

        return bean;
    }

    public <U> U createWiredBean(Class<U> beanClass) throws Exception
    {
        if (autoWiringFactory == null)
        {
            throw new UnsupportedOperationException("Unable to create beans at this stage.");
        }

        U bean = (U) autoWiringFactory.autowire(beanClass, autowireStrategy, false);

        if (bean instanceof ApplicationContextAware)
        {
            ((ApplicationContextAware) bean).setApplicationContext(context);
        }

        // not sure why spring does not call these methods itself...
        if (bean instanceof InitializingBean)
        {
            ((InitializingBean)bean).afterPropertiesSet();
        }

        return bean;
    }

    /**
     * Sets the autowiring strategy
     *
     * @param autowireStrategy identifier
     *
     * @see AutowireCapableBeanFactory#AUTOWIRE_BY_NAME
     * @see AutowireCapableBeanFactory#AUTOWIRE_BY_TYPE
     * @see AutowireCapableBeanFactory#AUTOWIRE_CONSTRUCTOR
     */
    public void setAutowireStrategy(int autowireStrategy)
    {
        switch (autowireStrategy)
        {
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
