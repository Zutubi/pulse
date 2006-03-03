package com.cinnamonbob.velocity;

import com.cinnamonbob.bootstrap.ApplicationConfiguration;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.opensymphony.webwork.views.velocity.VelocityManager;
import com.opensymphony.xwork.util.OgnlValueStack;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CustomVelocityManager extends VelocityManager
{
    public Context createContext(OgnlValueStack stack, HttpServletRequest req, HttpServletResponse res)
    {
        Context context = super.createContext(stack, req, res);
        ApplicationConfiguration config = ConfigUtils.getManager().getAppConfig();
        context.put("helpUrl", config.getHelpUrl());
        return context;
    }
}
