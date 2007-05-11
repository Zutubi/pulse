package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.webwork.PrototypeUtils;

/**
 * A descriptor for columns of delete action links.
 */
public class DeleteColumnDescriptor extends ActionColumnDescriptor
{
    private static final String ACTION = "delete";

    public DeleteColumnDescriptor(boolean ajax)
    {
        super(ACTION, ajax);
    }

    public Column instantiate(String path, Record value)
    {
        Column column = super.instantiate(path, value);
        if (isAjax())
        {
            column.setOnclick("deletePath");
        }
        else
        {
            column.setLink(PrototypeUtils.getConfigURL(path, "delete", "confirm"));
        }
        return column;
    }
}
