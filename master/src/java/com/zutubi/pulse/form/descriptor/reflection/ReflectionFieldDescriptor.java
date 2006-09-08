package com.zutubi.pulse.form.descriptor.reflection;

import com.zutubi.pulse.form.bean.BeanException;
import com.zutubi.pulse.form.bean.BeanSupport;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.ui.components.Component;
import com.zutubi.pulse.form.ui.components.TextComponent;
import com.zutubi.pulse.form.FieldType;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class ReflectionFieldDescriptor implements FieldDescriptor
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

    public Component createField()
    {
        if (FieldType.TEXT.equals(fieldType))
        {
            TextComponent c = new TextComponent();
            c.setName(getName());
            c.setLabel(getName() + ".label");
            c.setRequired(isRequired());
            applyParameters(c);
            return c;
        }
        throw new RuntimeException("Unsupported field type '" + type + "'");
    }

    private void applyParameters(TextComponent c)
    {
        // Sigh, it would be nice if we could treat everything as a string. That way, we just pass
        // the entire parameter map into the component.  That way, if something is defined, it is made
        // available in the template.  If typing is required, then either the types need to be defined
        // at the annotation level.. or they have to be defined in the object.  In annotations, it is
        // self documenting.

        for (Map.Entry<String, Object> entry : parameters.entrySet())
        {
            try
            {
                BeanSupport.setProperty(entry.getKey(), entry.getValue(), c);
            }
            catch (BeanException e)
            {
                // noop.
            }
        }
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }
}
