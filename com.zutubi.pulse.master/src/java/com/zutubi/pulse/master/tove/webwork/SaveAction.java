package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.TextUtils;

/**
 * Generic configuration save action.  Applies changes made to an existing
 * record.
 */
public class SaveAction extends ToveActionSupport
{
    private String symbolicName;

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public void doCancel()
    {
        String parentPath = PathUtils.getParentPath(path);
        if(isParentEmbeddedCollection(parentPath))
        {
            path = parentPath;
        }

        response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
    }

    public String execute() throws Exception
    {
        if (isSaveSelected())
        {
            return doSave();
        }
        else
        {
            return doRender();
        }
    }

    @SuppressWarnings({"unchecked"})
    private String doSave() throws Exception
    {
        if (!TextUtils.stringSet(symbolicName))
        {
            return doRender();
        }

        CompositeType type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            return doRender();
        }

        record = ToveUtils.toRecord(type, ActionContext.getContext().getParameters());

        String parentPath = PathUtils.getParentPath(path);
        String baseName = PathUtils.getBaseName(path);
        try
        {
            Configuration instance = configurationTemplateManager.validate(parentPath, baseName, record, configurationTemplateManager.isConcrete(path), false);
            if (!instance.isValid())
            {
                ToveUtils.mapErrors(instance, this, null);
            }
        }
        catch (TypeException e)
        {
            addActionError(e.getMessage());
        }

        if(hasErrors())
        {
            return INPUT;
        }

        String newPath = configurationTemplateManager.saveRecord(path, (MutableRecord) record);

        if(isParentEmbeddedCollection(parentPath))
        {
            path = PathUtils.getParentPath(newPath);
            response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
        }
        else
        {
            response = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
            if(!newPath.equals(path))
            {
                String newDisplayName = ToveUtils.getDisplayName(newPath, configurationTemplateManager);
                response.addRenamedPath(new ConfigurationResponse.Rename(path, newPath, newDisplayName));
            }

            path = newPath;
        }

        return doRender();
    }

    private boolean isParentEmbeddedCollection(String parentPath)
    {
        if(parentPath == null)
        {
            return false;
        }

        ComplexType parentType = configurationTemplateManager.getType(parentPath);
        return ToveUtils.isEmbeddedCollection(parentType);
    }
}
