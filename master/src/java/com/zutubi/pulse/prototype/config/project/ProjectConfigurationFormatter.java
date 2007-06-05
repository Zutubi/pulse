package com.zutubi.pulse.prototype.config.project;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;

/**
 *
 *
 */
public class ProjectConfigurationFormatter
{
    private MasterConfigurationManager configurationManager;

    public String getName(ProjectConfiguration config)
    {
        String base = configurationManager.getSystemConfig().getContextPathNormalised();

        return "<a href=\""+ base + "/config/" + config.getConfigurationPath()+"\">" + config.getName() + "</a>";
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
