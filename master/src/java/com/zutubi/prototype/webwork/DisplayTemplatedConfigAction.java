package com.zutubi.prototype.webwork;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.web.ActionSupport;
import com.opensymphony.util.TextUtils;

/**
 * This action provides support for rendering the admins configuration pages.
 *
 */
// Again, a bad name.  It will have to remain for now...
public class DisplayTemplatedConfigAction extends ActionSupport
{
    private ConfigurationTemplateManager configurationTemplateManager;

    /**
     * The full path being displayed.
     */
    private String path;

    /**
     * The name of the base entity being displayed.  For example, the name of the project or agent.
     */
    private String baseName;

    private String scope;

    private String configTreePath;

    private String templateTreePath;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getBaseName()
    {
        return baseName;
    }

    public String getScope()
    {
        return scope;
    }

    public String getConfigTreePath()
    {
        return configTreePath;
    }

    public String getTemplateTreePath()
    {
        return templateTreePath;
    }

    public void validate()
    {
        if (!TextUtils.stringSet(path))
        {
            addFieldError("path", "path.required");
        }
    }

    public String execute() throws Exception
    {
        if (hasErrors())
        {
            return INPUT;
        }

        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements.length > 0)
        {
            // extract the scope.
            scope = pathElements[0];
        }

        // if we have more than just the scope, evaluate the config tree and template tree paths.
        if (pathElements.length > 1)
        {
            baseName = pathElements[1]; // the base name identifies the project/agent - scope level configuration.

            String[] configPath = new String[pathElements.length - 1];
            System.arraycopy(pathElements, 1, configPath, 0, configPath.length);
            configTreePath = PathUtils.getPath(configPath);

            // the template path is based on the scope/baseName.
            templateTreePath = configurationTemplateManager.getTemplatePath(PathUtils.getPath(scope, baseName));
        }
        
        return SUCCESS;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
