package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;

/**
 *
 *
 */
public class SummaryColumnDescriptor extends ColumnDescriptor
{
    private TypeRegistry typeRegistry;

    public Column instantiate(Record record)
    {
        try
        {
            Type type = typeRegistry.getType(record.getSymbolicName());
            Object instance = type.instantiate(record);

            Column c = new Column();
            c.setSpan(colspan);
            c.addAll(getParameters());
            c.setValue(formatter.format(instance));
            return c;

        }
        catch (TypeException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
