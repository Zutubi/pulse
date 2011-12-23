package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.StringUtils;

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

    public CompositeType getType()
    {
        if (type == null && StringUtils.stringSet(symbolicName))
        {
            type = typeRegistry.getType(symbolicName);
        }

        return (CompositeType) type;
    }

    public void doCancel()
    {
        if(isParentEmbeddedCollection())
        {
            path = PathUtils.getParentPath(path);
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
    
    protected String doSave() throws Exception
    {
        CompositeType type = getType();
        if (type == null)
        {
            return doRender();
        }
        
        bindRecord();
        
        if (validateRecord())
        {
            setupResponse(configurationTemplateManager.saveRecord(path, (MutableRecord) record));
            return doRender();            
        }
        else
        {
            return INPUT;
        }
    }

    @SuppressWarnings({"unchecked"})
    protected void bindRecord()
    {
        CompositeType type = getType();
        record = ToveUtils.toRecord(type, ActionContext.getContext().getParameters());
        Record existingRecord = configurationTemplateManager.getRecord(path);
        if (existingRecord != null)
        {
            ToveUtils.unsuppressPasswords(existingRecord, (MutableRecord) record, type, false);
        }
    }

    protected boolean validateRecord()
    {
        try
        {
            String parentPath = PathUtils.getParentPath(path);
            String baseName = PathUtils.getBaseName(path);
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
        
        return !hasErrors();
    }

    protected void setupResponse(String newPath)
    {
        if (isParentEmbeddedCollection())
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
                String collapsedCollection = ToveUtils.getCollapsedCollection(newPath, type, configurationSecurityManager);
                response.addRenamedPath(new ConfigurationResponse.Rename(path, newPath, newDisplayName, collapsedCollection));
            }

            path = newPath;
        }
    }
}
