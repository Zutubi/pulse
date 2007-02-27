package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;

import java.util.Map;
import java.util.HashMap;

/**
 *
 *
 */
public abstract class ColumnDescriptor
{
    private String name;

    protected int colspan = 1;

    protected Formatter formatter = new SimpleColumnFormatter();

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public void addParameter(String key, Object value)
    {
        parameters.put(key, value);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void setFormatter(Formatter formatter)
    {
        this.formatter = formatter;
    }

    public abstract Column instantiate(Record value);

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setColspan(int colspan)
    {
        this.colspan = colspan;
    }
}
