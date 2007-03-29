package com.zutubi.prototype;

import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.type.record.Record;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class FieldDescriptor implements Descriptor
{
    private Map<String, Object> parameters = new HashMap<String, Object>();

    public String getName()
    {
        return (String) parameters.get("name");
    }

    public void setName(String name)
    {
        this.parameters.put("name", name);
    }

    public void addParameter(String key, Object value)
    {
        this.parameters.put(key, value);
    }

    public void addAll(Map<String, Object> parameters)
    {
        this.parameters.putAll(parameters);
    }

    public Object getParameter(String key)
    {
        return this.parameters.get(key);
    }

    public Map<String, Object> getParameters()
    {
        return this.parameters;
    }

    public void setParameters(Map<String, Object> parameters)
    {
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

    public Field instantiate(Record instance)
    {
        Field field = new Field();
        field.setName(getName());
        field.setId(getName());
        field.setLabel(getName() + ".label");
        field.addAll(getParameters());

        if (field.getValue() == null)
        {
            if (instance != null)
            {
                field.setValue(instance.get(getName()));
            }
        }

        return field;
    }

    public void setType(String type)
    {
        parameters.put("type", type);
    }

    public String getType()
    {
        return (String) parameters.get("type");
    }
}
