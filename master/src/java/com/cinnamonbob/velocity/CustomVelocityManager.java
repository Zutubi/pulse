package com.cinnamonbob.velocity;

import com.cinnamonbob.bootstrap.ApplicationConfiguration;
import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.security.AcegiUtils;
import com.cinnamonbob.Version;
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

    private UserManager userManager;

    public CustomVelocityManager()
    {

    }

    public Context createContext(OgnlValueStack stack, HttpServletRequest req, HttpServletResponse res)
    {
        Context context = super.createContext(stack, req, res);
        ApplicationConfiguration config = configManager.getAppConfig();
        context.put("helpUrl", config.getHelpUrl());

        String login = AcegiUtils.getLoggedInUser();
        if (login != null && getUserManager() != null)
        {
            User user = getUserManager().getUser(login);
            context.put("principle", user);
        }

        // add version strings.
        context.put("version_number", Version.getVersion());
        context.put("build_date", Version.getBuildDate());
        context.put("build_number", Version.getBuildNumber());

        return context;
    }

    public void setConfigurationManager(ConfigurationManager configManager)
    {
        this.configManager = configManager;
    }

    // HACK: the autowiring does not work correctly when the app is first setup - the
    //       context does not contain a user manager instance when this 'singleton' is
    //       first created. SOO, we need to help it out a little.
    public UserManager getUserManager()
    {
        if (userManager == null)
        {
            userManager = (UserManager) ComponentContext.getBean("userManager");
        }
        return userManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
