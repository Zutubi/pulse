/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.spring;

import org.springframework.context.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.Locale;
import java.io.IOException;

/**
 * This delegating application context allows us to give a reference to the systems
 * application context and then later change/update the delegate, ensuring that any
 * references still held to the application context contain the latest data.
 */
public class DelegatingApplicationContext implements ConfigurableApplicationContext
{
    private ConfigurableApplicationContext delegate;

    public DelegatingApplicationContext()
    {
    }

    public DelegatingApplicationContext(ConfigurableApplicationContext delegate)
    {
        this.delegate = delegate;
    }

    public void setDelegate(ConfigurableApplicationContext delegate)
    {
        this.delegate = delegate;
    }

    public ApplicationContext getDelegate()
    {
        return delegate;
    }

    public String getDisplayName()
    {
        return delegate.getDisplayName();
    }

    public ApplicationContext getParent()
    {
        if (delegate != null)
        {
            return delegate.getParent();
        }
        return null;
    }

    public long getStartupDate()
    {
        return delegate.getStartupDate();
    }

    public void publishEvent(ApplicationEvent event)
    {
        delegate.publishEvent(event);
    }

    public boolean containsBeanDefinition(String beanName)
    {
        return delegate.containsBeanDefinition(beanName);
    }

    public int getBeanDefinitionCount()
    {
        return delegate.getBeanDefinitionCount();
    }

    public String[] getBeanDefinitionNames()
    {
        return delegate.getBeanDefinitionNames();
    }

    public String[] getBeanDefinitionNames(Class type)
    {
        return delegate.getBeanDefinitionNames(type);
    }

    public String[] getBeanNamesForType(Class type)
    {
        return delegate.getBeanNamesForType(type);
    }

    public String[] getBeanNamesForType(Class type, boolean includePrototypes, boolean includeFactoryBeans)
    {
        return delegate.getBeanNamesForType(type, includePrototypes,  includeFactoryBeans);
    }

    public Map getBeansOfType(Class type) throws BeansException
    {
        return delegate.getBeansOfType(type);
    }

    public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans) throws BeansException
    {
        return delegate.getBeansOfType(type, includePrototypes, includeFactoryBeans);
    }

    public boolean containsBean(String name)
    {
        return delegate.containsBean(name);
    }

    public String[] getAliases(String name) throws NoSuchBeanDefinitionException
    {
        return delegate.getAliases(name);
    }

    public Object getBean(String name) throws BeansException
    {
        return delegate.getBean(name);
    }

    public Object getBean(String name, Class requiredType) throws BeansException
    {
        return delegate.getBean(name, requiredType);
    }

    public Class getType(String name) throws NoSuchBeanDefinitionException
    {
        return delegate.getType(name);
    }

    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException
    {
        return delegate.isSingleton(name);
    }

    public BeanFactory getParentBeanFactory()
    {
        return delegate.getParentBeanFactory();
    }

    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale)
    {
        return delegate.getMessage(code, args, defaultMessage, locale);
    }

    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException
    {
        return delegate.getMessage(code, args, locale);
    }

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException
    {
        return delegate.getMessage(resolvable, locale);
    }

    public Resource[] getResources(String locationPattern) throws IOException
    {
        return delegate.getResources(locationPattern);
    }

    public Resource getResource(String location)
    {
        return delegate.getResource(location);
    }

    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor processor)
    {
        delegate.addBeanFactoryPostProcessor(processor);
    }

    public void close()
    {
        delegate.close();
    }

    public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException
    {
        return delegate.getBeanFactory();
    }

    public void refresh() throws BeansException, IllegalStateException
    {
        delegate.refresh();
    }

    public void setParent(ApplicationContext context)
    {
        delegate.setParent(context);
    }

    public void closeAll()
    {
        ConfigurableApplicationContext parent = (ConfigurableApplicationContext) delegate.getParent();
        while (parent != null)
        {
            parent.close();
            parent = (ConfigurableApplicationContext) parent.getParent();
        }
    }
}
