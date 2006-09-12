package com.zutubi.pulse.form.descriptor;

import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.ActionDescriptor;
import com.zutubi.pulse.form.descriptor.DefaultActionDescriptor;

import java.util.*;

/**
 * <class-comment/>
 */
public class DefaultFormDescriptor implements FormDescriptor
{
    private List<FieldDescriptor> fieldDescriptors;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    private Class type;

    public Class getType()
    {
        return type;
    }

    public void setType(Class type)
    {
        this.type = type;
    }

    public List<FieldDescriptor> getFieldDescriptors()
    {
        return fieldDescriptors;
    }

    public void setFieldDescriptors(List<FieldDescriptor> fieldDescriptors)
    {
        this.fieldDescriptors = fieldDescriptors;
    }

    public FieldDescriptor getFieldDescriptor(String name)
    {
        for (FieldDescriptor descriptor : fieldDescriptors)
        {
            if (descriptor.getName().equals(name))
            {
                return descriptor;
            }
        }
        return null;
    }

    public List<String> getFieldOrder()
    {
        return null;
    }

    public List<ActionDescriptor> getActionDescriptors()
    {
        return Arrays.asList((ActionDescriptor)
                new DefaultActionDescriptor(ActionDescriptor.SAVE),
                new DefaultActionDescriptor(ActionDescriptor.CANCEL),
                new DefaultActionDescriptor(ActionDescriptor.RESET)
        );
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

}
