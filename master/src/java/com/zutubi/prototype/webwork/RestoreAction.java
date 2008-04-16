package com.zutubi.prototype.webwork;

import com.zutubi.prototype.type.ComplexType;
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
        String displayName = PrototypeUtils.getDisplayName(path, configurationTemplateManager);
 	    ComplexType type = configurationTemplateManager.getType(path, ComplexType.class);
 	    boolean leaf = PrototypeUtils.isLeaf(path, configurationTemplateManager, configurationSecurityManager);
 	    String iconCls = PrototypeUtils.getIconCls(type);
 	    response.addAddedFile(new ConfigurationResponse.Addition(path, displayName, null, iconCls, leaf, false));
        path = response.getNewPath();
        return SUCCESS;
    }
}
