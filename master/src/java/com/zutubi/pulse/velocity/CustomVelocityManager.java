package com.zutubi.pulse.velocity;

import com.opensymphony.webwork.views.velocity.VelocityManager;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.security.AcegiUtils;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CustomVelocityManager extends VelocityManager
{
    private MasterConfigurationManager configManager;

    private UserManager userManager;

    public CustomVelocityManager()
    {

    }

    public Context createContext(OgnlValueStack stack, HttpServletRequest req, HttpServletResponse res)
    {
        Context context = super.createContext(stack, req, res);
        MasterConfiguration config = configManager.getAppConfig();
        context.put("helpUrl", config.getHelpUrl());
        context.put("rssEnabled", config.getRssEnabled());

        String login = AcegiUtils.getLoggedInUser();
        if (login != null && getUserManager() != null)
        {
            User user = getUserManager().getUser(login);
            context.put("principle", user);
            context.put("canLogout", AcegiUtils.canLogout());
        }

        // add version strings.
        Version v = Version.getVersion();
        context.put("version_number", v.getVersionNumber());
        context.put("build_date", v.getBuildDate());
        context.put("build_number", v.getBuildNumber());

        context.put("license", LicenseHolder.getLicense());

        return context;
    }

    public void setConfigurationManager(MasterConfigurationManager configManager)
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

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
