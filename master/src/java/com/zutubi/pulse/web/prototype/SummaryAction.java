package com.zutubi.pulse.web.prototype;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.prototype.ProjectConfigurationManager;
import com.zutubi.prototype.model.Config;

import java.util.List;
import java.util.Arrays;

/**
 *
 *
 */
public class SummaryAction extends ActionSupport
{
    private String scope;

    private ProjectConfigurationManager projectConfigurationManager;

    private Config config;

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public String getScope()
    {
        return scope;
    }

    public Config getConfig()
    {
        return config;
    }

    public String execute() throws Exception
    {
        // use the scope to identify the configuration data.

        // load the root level configuration from the project configuration manager..
        config = new Config();
        for (String s : projectConfigurationManager.getProjectConfigurationRoot())
        {
            config.addNestedProperty(s);
        }

        return SUCCESS;
    }

    public void setProjectConfigurationManager(ProjectConfigurationManager projectConfigurationManager)
    {
        this.projectConfigurationManager = projectConfigurationManager;
    }
}
