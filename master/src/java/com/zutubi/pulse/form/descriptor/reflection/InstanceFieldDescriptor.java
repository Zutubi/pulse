package com.zutubi.pulse.form.descriptor.reflection;

import com.zutubi.pulse.form.descriptor.DefaultFieldDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.InstanceDescriptor;
import com.zutubi.pulse.form.descriptor.Descriptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

/**
 * <class-comment/>
 */
public class InstanceFieldDescriptor extends DefaultFieldDescriptor implements InstanceDescriptor
{
    private Method readMethod;

    public InstanceFieldDescriptor(FieldDescriptor baseDescriptor)
    {
        setFieldType(baseDescriptor.getFieldType());
        setName(baseDescriptor.getName());
        setRequired(baseDescriptor.isRequired());
        setType(baseDescriptor.getType());
    }

    public void setReadMethod(Method readMethod)
    {
        this.readMethod = readMethod;
    }

    public Descriptor setInstance(Object instance)
    {
        // make a copy, modify it, and return it.
        DefaultFieldDescriptor instanceDescriptor = new DefaultFieldDescriptor();
        instanceDescriptor.setFieldType(getFieldType());
        instanceDescriptor.setName(getName());
        instanceDescriptor.setRequired(isRequired());
        instanceDescriptor.setType(getType());

        try
        {
            Object list = readMethod.invoke(instance);
            if (list != null)
            {
                instanceDescriptor.getParameters().put("list", list);
            }
            else
            {
                instanceDescriptor.getParameters().put("list", Collections.EMPTY_LIST);
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return instanceDescriptor;
    }
}
