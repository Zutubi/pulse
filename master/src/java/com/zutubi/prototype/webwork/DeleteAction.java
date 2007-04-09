package com.zutubi.prototype.webwork;

import com.zutubi.prototype.type.record.PathUtils;

/**
 *
 *
 */
public class DeleteAction extends PrototypeSupport
{
    public String execute() throws Exception
    {
        if (!isDeleteSelected())
        {
            return ERROR;
        }

        configurationPersistenceManager.delete(path);

        path = PathUtils.getParentPath(path);

        return SUCCESS;
    }
}
