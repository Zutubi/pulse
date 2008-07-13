package com.zutubi.tove.webwork.help;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CompositeType;

/**
 * Displays documentation for template operations.
 */
public class TemplateHelpAction extends ActionSupport
{
    private String path;
    private ConfigurationTemplateManager configurationTemplateManager;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws Exception
    {
        CompositeType type = configurationTemplateManager.getType(path, CompositeType.class);
        return SUCCESS;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
