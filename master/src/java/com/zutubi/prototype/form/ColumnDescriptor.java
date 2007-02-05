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

    public Object instantiate(int index, Object value)
    {
        Column c = new Column();
        c.setParameters(getParameters());
        c.setValue(formatter.format(index, value));
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
}
