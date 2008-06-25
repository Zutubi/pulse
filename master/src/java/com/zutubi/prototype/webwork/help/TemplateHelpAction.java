package com.zutubi.prototype.webwork.help;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.pulse.web.ActionSupport;

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
