package com.zutubi.pulse.master.tove.wizard.webwork;

import com.zutubi.pulse.master.tove.classification.ClassificationManager;
import com.zutubi.pulse.master.tove.webwork.ConfigurationErrors;
import com.zutubi.pulse.master.tove.webwork.ConfigurationPanel;
import com.zutubi.pulse.master.tove.webwork.ConfigurationResponse;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.type.record.PathUtils;

/**
 *
 *
 */
public class AjaxEnabledConfigurationWizardAction extends ConfigurationWizardAction
{
    private static final String TEMPLATE = "aconfig/wizard.vm";

    private ConfigurationPanel configurationPanel;
    private ConfigurationResponse configurationResponse;
    private ConfigurationErrors configurationErrors;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ClassificationManager classificationManager;

    public ConfigurationPanel getConfigurationPanel()
    {
        return configurationPanel;
    }

    public ConfigurationResponse getConfigurationResponse()
    {
        return configurationResponse;
    }

    public ConfigurationErrors getConfigurationErrors()
    {
        if(configurationErrors == null)
        {
            configurationErrors = new ConfigurationErrors(this);
        }
        return configurationErrors;
    }

    public String execute()
    {
        String result = super.execute();

        String newPath = getPath();
        String parentPath = PathUtils.getParentPath(newPath);

        if (SUCCESS.equals(result))
        {
            if(isEmbeddedCollection(parentPath))
            {
                configurationResponse = new ConfigurationResponse(parentPath, configurationTemplateManager.getTemplatePath(parentPath));
            }
            else
            {
                configurationResponse = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
                if(originalPath.equals(newPath))
                {
                    // We configured a singleton, and may have relabelled.
                    String newDisplayName = ToveUtils.getDisplayName(newPath, configurationTemplateManager);
                    String collapsedCollection = ToveUtils.getCollapsedCollection(newPath, configurationTemplateManager.getType(newPath), configurationSecurityManager);
                    configurationResponse.addRenamedPath(new ConfigurationResponse.Rename(originalPath, newPath, newDisplayName, collapsedCollection));
                }
                else
                {
                    // Then we just added this path
                    configurationResponse.registerNewPathAdded(configurationTemplateManager, configurationSecurityManager, classificationManager);
                }
            }
        }
        else if ("step".equals(result))
        {
            configurationPanel = new ConfigurationPanel(TEMPLATE);
        }
        else if ("input".equals(result))
        {
            configurationErrors = new ConfigurationErrors(this);
        }
        else if ("cancel".equals(result))
        {
            if(isEmbeddedCollection(parentPath))
            {
                configurationResponse = new ConfigurationResponse(parentPath, configurationTemplateManager.getTemplatePath(parentPath));
            }
            else
            {
                configurationResponse = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
            }
        }
        return result;
    }

    private boolean isEmbeddedCollection(String insertPath)
    {
        return insertPath != null && ToveUtils.isEmbeddedCollection(configurationTemplateManager.getType(insertPath));
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setClassificationManager(ClassificationManager classificationManager)
    {
        this.classificationManager = classificationManager;
    }
}
