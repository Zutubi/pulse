package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.record.PathUtils;

/**
 * Action for restoring a hidden record.
 */
public class RestoreAction extends ToveActionSupport
{
    public String execute() throws Exception
    {
        String parentPath = PathUtils.getParentPath(path);

        configurationTemplateManager.restore(path);
        response = new ConfigurationResponse(parentPath, null);
        String displayName = ToveUtils.getDisplayName(path, configurationTemplateManager);
 	    ComplexType type = configurationTemplateManager.getType(path, ComplexType.class);
 	    boolean leaf = ToveUtils.isLeaf(path, configurationTemplateManager, configurationSecurityManager);
 	    String iconCls = ToveUtils.getIconCls(type);
 	    response.addAddedFile(new ConfigurationResponse.Addition(path, displayName, null, iconCls, leaf, false));
        path = response.getNewPath();
        return SUCCESS;
    }
}
