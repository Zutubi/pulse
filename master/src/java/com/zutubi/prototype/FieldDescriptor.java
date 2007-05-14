package com.zutubi.prototype;

import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.Record;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class FieldDescriptor extends AbstractDescriptor
{
    private FormDescriptor form;
    private Object value;
    private String path;
    private TypeProperty property;
    private String name;
    private String type;
    private boolean required;
    private boolean constrained;
    private List<String> scripts = new LinkedList<String>();

    public Field instantiate(String path, Record instance)
    {
        Field field = new Field();
        field.setType(getType());
        field.setName(getName());
        field.setId("zfid." + getName());
        field.setLabel(getName() + ".label");
        
        field.addParameter("path", getPath());
        field.addParameter("required", isRequired());
        field.addParameter("constrained", isConstrained());
        field.addParameter("property", getProperty());
        field.addParameter("scripts", getScripts());
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

    public FormDescriptor getForm()
    {
        return form;
    }

    public void setForm(FormDescriptor form)
    {
        this.form = form;
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

    public void addScript(String template)
    {
        scripts.add(template);
    }

    public List<String> getScripts()
    {
        return scripts;
    }
}
