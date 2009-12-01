package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.pulse.master.tove.classification.ClassificationManager;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.record.PathUtils;

/**
 * Action for restoring a hidden record.
 */
public class RestoreAction extends ToveActionSupport
{
    private ClassificationManager classificationManager;

    public String execute() throws Exception
    {
        String parentPath = PathUtils.getParentPath(path);

        configurationTemplateManager.restore(path);
        response = new ConfigurationResponse(parentPath, null);
        String displayName = ToveUtils.getDisplayName(path, configurationTemplateManager);
 	    ComplexType type = configurationTemplateManager.getType(path, ComplexType.class);
        String collapsedCollection = ToveUtils.getCollapsedCollection(path, type, configurationSecurityManager);
        boolean leaf = ToveUtils.isLeaf(path, configurationTemplateManager, configurationSecurityManager);
        String iconCls = ToveUtils.getIconCls(path, classificationManager);
 	    response.addAddedFile(new ConfigurationResponse.Addition(path, displayName, null, collapsedCollection, iconCls, leaf, false));
        path = response.getNewPath();
        return SUCCESS;
    }

    public void setClassificationManager(ClassificationManager classificationManager)
    {
        this.classificationManager = classificationManager;
    }
}
