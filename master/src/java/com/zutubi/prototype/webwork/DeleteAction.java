package com.zutubi.prototype.webwork;

import com.zutubi.prototype.config.ReferenceCleanupTask;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.validation.i18n.DefaultTextProvider;
import com.zutubi.validation.i18n.TextProvider;

/**
 * Action for deleting a record.  Also handles displaying confirmation when
 * necessary.
 */
public class DeleteAction extends PrototypeSupport
{
    private ReferenceCleanupTask task;

    public ReferenceCleanupTask getTask()
    {
        return task;
    }

    public TextProvider getTextProvider()
    {
        return new DefaultTextProvider();
    }
    
    public String execute() throws Exception
    {
        if(isConfirmSelected())
        {
            task = configurationPersistenceManager.getCleanupTasks(getPath());
            return "confirm";
        }
        else if (isDeleteSelected())
        {
            configurationPersistenceManager.delete(path);
            path = PathUtils.getParentPath(path);
            return SUCCESS;
        }
        else if(isCancelSelected())
        {
            path = PathUtils.getParentPath(path);
            return "cancel";
        }
        
        return ERROR;
    }
}
