package com.zutubi.pulse.form.descriptor;

import com.zutubi.pulse.form.FieldType;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.ui.components.Component;
import com.zutubi.pulse.form.ui.components.TextComponent;
import com.zutubi.validation.bean.BeanException;
import com.zutubi.validation.bean.BeanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class DefaultFieldDescriptor implements FieldDescriptor
{
    private boolean required;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    private Class type;

    private String name;

    private String fieldType;

    public Class getType()
    {
        return type;
    }

    public void setType(Class type)
    {
        this.type = type;
    }

    public String getFieldType()
    {
        return fieldType;
    }

    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }
}
