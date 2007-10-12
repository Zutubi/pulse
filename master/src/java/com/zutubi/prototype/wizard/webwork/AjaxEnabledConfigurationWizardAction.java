package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.webwork.ConfigurationErrors;
import com.zutubi.prototype.webwork.ConfigurationPanel;
import com.zutubi.prototype.webwork.ConfigurationResponse;
import com.zutubi.prototype.webwork.PrototypeUtils;

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
                if(!originalPath.equals(newPath))
                {
                    // Then we added something.
                    String displayName = PrototypeUtils.getDisplayName(newPath, configurationTemplateManager);
                    configurationResponse.addAddedFile(new ConfigurationResponse.Addition(newPath, displayName, configurationTemplateManager.getTemplatePath(newPath), PrototypeUtils.isLeaf(newPath, configurationTemplateManager, configurationSecurityManager)));
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
        return insertPath != null && PrototypeUtils.isEmbeddedCollection(configurationTemplateManager.getType(insertPath));
    }
}
