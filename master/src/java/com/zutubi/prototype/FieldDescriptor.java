package com.zutubi.prototype;

import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.Record;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class FieldDescriptor implements Descriptor
{
    private static final String KEY_PATH = "path";
    private static final String KEY_PROPERTY = "property";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public String getPath()
    {
        return (String) parameters.get(KEY_PATH);
    }

    public void setPath(String path)
    {
        this.parameters.put(KEY_PATH, path);
    }

    public TypeProperty getProperty()
    {
        return (TypeProperty) parameters.get(KEY_PROPERTY);
    }

    public void setProperty(TypeProperty property)
    {
        this.parameters.put(KEY_PROPERTY, property);
    }

    public String getName()
    {
        return (String) parameters.get(KEY_NAME);
    }

    public void setName(String name)
    {
        this.parameters.put(KEY_NAME, name);
    }

    public void addParameter(String key, Object value)
    {
        this.parameters.put(key, value);
    }

    public void addAll(Map<String, Object> parameters)
    {
        this.parameters.putAll(parameters);
    }

    public boolean hasParameter(String key)
    {
        return parameters.containsKey(key);
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

    public Field instantiate(String path, Record instance)
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
        parameters.put(KEY_TYPE, type);
    }

    public String getType()
    {
        return (String) parameters.get(KEY_TYPE);
    }
}
