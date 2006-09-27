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

    private List<ActionDescriptor> actionDescriptors = new LinkedList<ActionDescriptor>();

    private Class type;

    private String[] fieldOrder;

    public DefaultFormDescriptor()
    {
        // default values.
        actionDescriptors = Arrays.asList((ActionDescriptor)
                new DefaultActionDescriptor(ActionDescriptor.SAVE),
                new DefaultActionDescriptor(ActionDescriptor.CANCEL),
                new DefaultActionDescriptor(ActionDescriptor.RESET)
        );
    }

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

    public void addFieldDescriptor(FieldDescriptor descriptor)
    {
        fieldDescriptors.add(descriptor);
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

    public String[] getFieldOrder()
    {
        return this.fieldOrder;
    }

    public void setFieldOrder(String[] order)
    {
        this.fieldOrder = order;
    }

    public List<ActionDescriptor> getActionDescriptors()
    {
        return this.actionDescriptors;
    }

    public void setActionDescriptors(List<ActionDescriptor> actionDescriptors)
    {
        this.actionDescriptors = actionDescriptors;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

}
