package com.zutubi.prototype.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.type.ComplexType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.TextUtils;

/**
 * Generic configuration save action.  Applies changes made to an existing
 * record.
 */
public class SaveAction extends PrototypeSupport
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
        Type parentType = configurationTemplateManager.getType(parentPath);
        if(PrototypeUtils.isEmbeddedCollection(parentType))
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

        record = PrototypeUtils.toRecord(type, ActionContext.getContext().getParameters());

        String parentPath = PathUtils.getParentPath(path);
        String baseName = PathUtils.getBaseName(path);
        try
        {
            Configuration instance = configurationTemplateManager.validate(parentPath, baseName, record, configurationTemplateManager.isConcrete(path), false);
            if (!instance.isValid())
            {
                PrototypeUtils.mapErrors(instance, this, null);
            }
        }
        catch (TypeException e)
        {
            addActionError(e.getMessage());
        }

        if(hasErrors())
        {
            prepare();
            return INPUT;
        }

        String newPath = configurationTemplateManager.saveRecord(path, (MutableRecord) record);

        ComplexType parentType = configurationTemplateManager.getType(parentPath);
        if(PrototypeUtils.isEmbeddedCollection(parentType))
        {
            path = PathUtils.getParentPath(newPath);
            response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
        }
        else
        {
            response = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
            if(!newPath.equals(path))
            {
                String newDisplayName = PrototypeUtils.getDisplayName(newPath, configurationTemplateManager);
                response.addRenamedPath(new ConfigurationResponse.Rename(path, newPath, newDisplayName));
            }

            path = newPath;
        }

        return doRender();
    }
}
