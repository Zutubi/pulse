package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.validation.XWorkValidationAdapter;

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
        if (configurationTemplateManager.validate(parentPath, baseName, record, new XWorkValidationAdapter(this)) == null)
        {
            prepare();
            return INPUT;
        }

        String newPath = configurationTemplateManager.saveRecord(path, record);
        response = new ConfigurationResponse(newPath, configurationTemplateManager.getTemplatePath(newPath));
        if(!newPath.equals(path))
        {
            response.addRenamedPath(new ConfigurationResponse.Rename(path, newPath));
        }

        path = newPath;

        return doRender();
    }
}
