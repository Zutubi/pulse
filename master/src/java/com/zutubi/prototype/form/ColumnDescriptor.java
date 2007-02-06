package com.zutubi.prototype.form;

import com.zutubi.prototype.form.model.Column;

import java.util.Map;
import java.util.HashMap;

/**
 *
 *
 */
public class ColumnDescriptor
{
    private String name;

    private int colspan = 1;

    private ColumnFormatter formatter = new SimpleColumnFormatter();

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public void addParameter(String key, Object value)
    {
        parameters.put(key, value);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void setFormatter(ColumnFormatter formatter)
    {
        this.formatter = formatter;
    }

    public Column instantiate(Object value)
    {
        Column c = new Column();
        c.setSpan(colspan);
        c.setParameters(getParameters());
        c.setValue(formatter.format(value));
        return c;
    }

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
