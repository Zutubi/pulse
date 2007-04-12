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

    public AddColumnDescriptor()
    {
        super(ACTION);
    }

    public AddColumnDescriptor(int colspan)
    {
        super(ACTION, colspan);
    }

    public Column instantiate(String path, Record value)
    {
        Column column = super.instantiate(path, value);
        column.setLink(PrototypeUtils.getConfigURL(path, "wizard", null));
        return column;
    }
}
