package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.webwork.PrototypeUtils;

/**
 * A descriptor for columns of edit action links.
 */
public class EditColumnDescriptor extends ActionColumnDescriptor
{
    private static final String ACTION = "edit";

    public EditColumnDescriptor()
    {
        super(ACTION);
    }

    public EditColumnDescriptor(int colspan)
    {
        super(ACTION, colspan);
    }

    public Column instantiate(String path, Record value)
    {
        Column column = super.instantiate(path, value);
        column.setLink(PrototypeUtils.getConfigURL(path, "display", null));
        return column;
    }
}
