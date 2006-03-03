package com.cinnamonbob.velocity;

import com.cinnamonbob.bootstrap.ApplicationConfiguration;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.opensymphony.webwork.views.velocity.VelocityManager;
import com.opensymphony.xwork.util.OgnlValueStack;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CustomVelocityManager extends VelocityManager
{
    private ConfigurationManager configManager;

    public Context createContext(OgnlValueStack stack, HttpServletRequest req, HttpServletResponse res)
    {
        Context context = super.createContext(stack, req, res);
        ApplicationConfiguration config = configManager.getAppConfig();
        context.put("helpUrl", config.getHelpUrl());
        return context;
    }

    public void setConfigurationManager(ConfigurationManager configManager)
    {
        this.configManager = configManager;
    }
}
