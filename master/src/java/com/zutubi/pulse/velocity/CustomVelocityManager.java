package com.zutubi.pulse.velocity;

import com.opensymphony.webwork.views.velocity.VelocityManager;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.webwork.mapping.Urls;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CustomVelocityManager extends VelocityManager
{
    private ProjectManager projectManager;
    private AgentManager agentManager;
    private UserManager userManager;
    private ConfigurationProvider configurationProvider;

    public CustomVelocityManager()
    {

    }

    public Context createContext(OgnlValueStack stack, HttpServletRequest req, HttpServletResponse res)
    {
        Context context = super.createContext(stack, req, res);
        context.put("urls", new Urls((String) context.get("base")));
        
        if(getConfigurationProvider() != null)
        {
            GeneralAdminConfiguration config = getConfigurationProvider().get(GeneralAdminConfiguration.class);
            if (config != null)
            {
                context.put("helpUrl", config.getBaseHelpUrl());
                context.put("rssEnabled", config.isRssEnabled());
                context.put("config", config);
            }
        }

        String login = AcegiUtils.getLoggedInUsername();
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

        License license = LicenseHolder.getLicense();
        context.put("license", license);
        ProjectManager projectManager = getProjectManager();
        AgentManager agentManager = getAgentManager();
        context.put("licenseExceeded", projectManager != null && agentManager != null && license.isExceeded(projectManager.getProjectCount(), agentManager.getAgentCount(), getUserManager().getUserCount()));

        return context;
    }

    // HACK: the autowiring does not work correctly when the app is first setup - the
    //       context does not contain a user manager instance when this 'singleton' is
    //       first created. SOO, we need to help it out a little.
    public ProjectManager getProjectManager()
    {
        if (projectManager == null && ComponentContext.containsBean("projectManager"))
        {
            projectManager = (ProjectManager) ComponentContext.getBean("projectManager");
        }
        return projectManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public AgentManager getAgentManager()
    {
        if (agentManager == null && ComponentContext.containsBean("agentManager"))
        {
            agentManager = (AgentManager) ComponentContext.getBean("agentManager");
        }
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

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

    private ConfigurationProvider getConfigurationProvider()
    {
        if(configurationProvider == null && ComponentContext.containsBean("configurationProvider"))
        {
            configurationProvider = ComponentContext.getBean("configurationProvider");
        }
        return configurationProvider;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
