package com.zutubi.prototype.webwork;

import com.zutubi.prototype.type.record.PathUtils;

/**
 * Action for restoring a hidden record.
 */
public class RestoreAction extends PrototypeSupport
{
    public String execute() throws Exception
    {
        String parentPath = PathUtils.getParentPath(path);

        configurationTemplateManager.restore(path);
        response = new ConfigurationResponse(parentPath, null);
        response.registerNewPathAdded(configurationTemplateManager, configurationSecurityManager);
        path = response.getNewPath();
        return SUCCESS;
    }
}
