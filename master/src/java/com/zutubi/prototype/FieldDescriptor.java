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
    private Map<String, Object> parameters = new HashMap<String, Object>();

    private Object value;
    private String path;
    private TypeProperty property;
    private String name;
    private String type;
    private boolean required;
    private boolean constrained;

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
        field.setType(getType());
        field.setName(getName());
        field.setId(getName());
        field.setLabel(getName() + ".label");
        
        field.addParameter("path", getPath());
        field.addParameter("required", isRequired());
        field.addParameter("constrained", isConstrained());
        field.addParameter("property", getProperty());
        field.addAll(getParameters());

        // if we do not have a value set, then take the value from the instance.
        if (value != null)
        {
            field.setValue(value);
        }
        else if (instance != null)
        {
            field.setValue(instance.get(getName()));
        }

        return field;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public TypeProperty getProperty()
    {
        return property;
    }

    public void setProperty(TypeProperty property)
    {
        this.property = property;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public boolean isConstrained()
    {
        return constrained;
    }

    public void setConstrained(boolean constrained)
    {
        this.constrained = constrained;
    }
}
