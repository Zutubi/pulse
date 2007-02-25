package com.zutubi.prototype;

import com.zutubi.prototype.model.Field;
import ognl.Ognl;
import ognl.OgnlException;

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
        parameters.put("name", name);
    }

    public void addParameter(String key, Object value)
    {
        parameters.put(key, value);
    }

    public void addAll(Map<String, Object> parameters)
    {
        parameters.putAll(parameters);
    }

    public Object getParameter(String key)
    {
        return parameters.get(key);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters)
    {
        this.parameters = parameters;
    }

    public Field instantiate(Object instance)
    {
        Field field = new Field();
        field.setName(getName());
        field.setId(getName());
        field.setLabel(getName() + ".label");
        field.addAll(getParameters());

        try
        {
            if (!field.getParameters().containsKey("value"))
            {
                if (instance != null)
                {
                    Map context = Ognl.createDefaultContext(instance);
                    field.setValue(Ognl.getValue(getName(), context, instance));
                }
            }
        }
        catch (OgnlException e)
        {
            field.setValue(e.getMessage());
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
