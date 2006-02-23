package com.cinnamonbob.servlet;

import com.cinnamonbob.bootstrap.ApplicationConfiguration;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.opensymphony.module.sitemesh.velocity.VelocityDecoratorServlet;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CustomVelocityDecoratorServlet extends VelocityDecoratorServlet
{
    public Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception
    {
        ApplicationConfiguration config = ConfigUtils.getManager().getAppConfig();
        context.put("helpUrl", config.getHelpUrl());
        return super.handleRequest(request, response, context);
    }
}
