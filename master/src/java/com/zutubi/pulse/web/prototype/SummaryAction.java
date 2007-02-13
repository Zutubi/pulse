package com.zutubi.pulse.web.prototype;

import com.zutubi.prototype.model.Config;
import com.zutubi.prototype.PrototypePath;
import com.zutubi.pulse.prototype.ProjectConfigurationManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 *
 *
 */
public class SummaryAction extends ActionSupport
{
    private ProjectConfigurationManager projectConfigurationManager;

    private Config config;

    private PrototypePath path;

    public String getPath()
    {
        return path.toString();
    }

    public void setPath(String path)
    {
        this.path = new PrototypePath(path);
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
