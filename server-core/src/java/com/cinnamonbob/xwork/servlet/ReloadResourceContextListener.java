package com.cinnamonbob.xwork.servlet;

import com.opensymphony.xwork.util.LocalizedTextUtil;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * <class-comment/>
 */
public class ReloadResourceContextListener implements ServletContextListener
{
    public static final String PROPERTY_KEY = "xwork.reload.bundles";

    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {

    }

    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        LocalizedTextUtil.setReloadBundles(Boolean.getBoolean(PROPERTY_KEY));
    }
}
