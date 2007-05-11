package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.webwork.PrototypeUtils;

/**
 * A descriptor for columns of add action links.
 */
public class AddColumnDescriptor extends ActionColumnDescriptor
{
    private static final String ACTION = "add";

    public AddColumnDescriptor(boolean ajax)
    {
        super(ACTION, ajax);
    }

    public AddColumnDescriptor(int colspan, boolean ajax)
    {
        super(ACTION, colspan,ajax);
    }

    public Column instantiate(String path, Record value)
    {
        Column column = super.instantiate(path, value);
        if (isAjax())
        {
            column.setOnclick("addToPath");
        }
        else
        {
            column.setLink(PrototypeUtils.getConfigURL(path, "wizard", null, isAjax()));
        }
        return column;
    }
}
