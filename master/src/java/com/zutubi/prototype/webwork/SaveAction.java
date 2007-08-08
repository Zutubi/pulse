package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;

/**
 *
 *
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

    public String execute() throws Exception
    {
        if (isSaveSelected())
        {
            return doSave();
        }
        else if(isCancelSelected())
        {
            response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
        }
        
        return doRender();
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
            Configuration instance = configurationTemplateManager.validate(parentPath, baseName, record, false);
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
        
        String displayName = PrototypeUtils.getDisplayName(path, configurationTemplateManager);
        String newPath = configurationTemplateManager.saveRecord(path, (MutableRecord) record);
        String newDisplayName = PrototypeUtils.getDisplayName(newPath, configurationTemplateManager);
        response = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
        if(!newPath.equals(path) || !newDisplayName.equals(displayName))
        {
            response.addRenamedPath(new ConfigurationResponse.Rename(path, newPath, newDisplayName));
        }

        path = newPath;

        return doRender();
    }
}
