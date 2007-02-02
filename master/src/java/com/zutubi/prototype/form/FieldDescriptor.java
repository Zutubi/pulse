package com.zutubi.prototype.form;

import com.zutubi.prototype.form.model.Field;

import java.util.HashMap;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

/**
 *
 *
 */
public class FieldDescriptor implements Descriptor
{
    private String name;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void addParameter(String key, Object value)
    {
        parameters.put(key, value);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters)
    {
        this.parameters = parameters;
    }

    public Field instantiate(Object obj)
    {
        Field field = new Field();
        field.getParameters().put("name", getName());
        field.getParameters().put("id", getName());
        field.getParameters().put("label", getName() + ".label");
        field.getParameters().putAll(getParameters());

        try
        {
            if (!field.getParameters().containsKey("value"))
            {
                Map context = Ognl.createDefaultContext(obj);
                field.getParameters().put("value", Ognl.getValue(getName(), context, obj));
            }
        }
        catch (OgnlException e)
        {
            field.getParameters().put("value", e.getMessage());
        }

        return field;
    }
}
