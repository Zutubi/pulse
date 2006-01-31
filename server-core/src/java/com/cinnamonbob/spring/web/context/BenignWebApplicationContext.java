package com.cinnamonbob.spring.web.context;

import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.BeansException;

import java.io.IOException;

/**
 * <class-comment/>
 */
public class BenignWebApplicationContext extends AbstractRefreshableWebApplicationContext
{
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException
    {
        // does not load anything....
    }
}
