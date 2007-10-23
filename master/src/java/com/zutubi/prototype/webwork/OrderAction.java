package com.zutubi.prototype.webwork;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.CollectionType;

import java.util.List;
import java.util.Collections;

/**
 * Action for restoring a hidden record.
 */
public class OrderAction extends PrototypeSupport
{
    private boolean moveUp = true;

    public void setMoveUp(boolean moveUp)
    {
        this.moveUp = moveUp;
    }

    public String execute() throws Exception
    {
        String parentPath = PathUtils.getParentPath(path);
        String baseName = PathUtils.getBaseName(path);

        Type parentType = configurationTemplateManager.getType(parentPath);
        if(!(parentType instanceof CollectionType))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': does not refer to a collection item");
        }

        CollectionType collectionType = (CollectionType) parentType;
        if(!collectionType.isOrdered())
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': parent collection is not ordered");
        }

        Record parentRecord = configurationTemplateManager.getRecord(parentPath);
        if(parentRecord == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': parent record does not exist");
        }

        List<String> order = collectionType.getOrder(parentRecord);
        int index = order.indexOf(baseName);
        if(index < 0)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': parent does not contain item '" + baseName + "'");
        }

        int otherIndex = moveUp ? index - 1 : index + 1;
        if(otherIndex >= 0 && otherIndex < order.size())
        {
            Collections.swap(order, index, otherIndex);
        }

        configurationTemplateManager.setOrder(parentPath, order);
        response = new ConfigurationResponse(parentPath, null);
        path = response.getNewPath();
        return SUCCESS;
    }
}
