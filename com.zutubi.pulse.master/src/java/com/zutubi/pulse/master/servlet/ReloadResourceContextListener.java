package com.zutubi.pulse.master.servlet;

import com.opensymphony.xwork.util.LocalizedTextUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ReloadResourceContextListener implements ServletContextListener
{
    public static final String PROPERTY_KEY = "xwork.reload.bundles";

    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {

    }

    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        boolean reload = Boolean.getBoolean(PROPERTY_KEY);
        LocalizedTextUtil.setReloadBundles(reload);
    }
}
