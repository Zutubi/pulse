package com.zutubi.prototype.form;

import com.zutubi.prototype.form.model.Field;
import com.zutubi.prototype.form.model.Form;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;

import java.util.*;

/**
 *
 *
 */
public class FormDescriptor implements Descriptor
{
    private List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();

    private Class type;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public Class getType()
    {
        return type;
    }

    public void setType(Class type)
    {
        this.type = type;
    }

    public void add(FieldDescriptor descriptor)
    {
        fieldDescriptors.add(descriptor);
    }

    public FieldDescriptor getFieldDescriptor(final String name)
    {
        return CollectionUtils.find(fieldDescriptors, new Predicate<FieldDescriptor>()
        {
            public boolean satisfied(FieldDescriptor fieldDescriptor)
            {
                return fieldDescriptor.getName().equals(name);
            }
        });
    }

    public List<FieldDescriptor> getFieldDescriptors()
    {
        return fieldDescriptors;
    }

    public void setFieldDescriptors(List<FieldDescriptor> fieldDescriptors)
    {
        this.fieldDescriptors = fieldDescriptors;
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

    public Form instantiate(Object data)
    {
        Form form = new Form();
        form.setId(type.getSimpleName());    

        List<String> fieldOrder = evaluateFieldOrder();

        int tabindex = 1;
        for (String fieldName : fieldOrder)
        {
            FieldDescriptor fieldDescriptor = getFieldDescriptor(fieldName);
            Field field = fieldDescriptor.instantiate(data);
            field.setTabindex(tabindex++);
            form.add(field);
        }
        
        return form;
    }

    protected List<String> evaluateFieldOrder()
    {
        // If a field order is defined, lets us it as the starting point.
        LinkedList<String> ordered = new LinkedList<String>();
        if (parameters.containsKey("fieldOrder"))
        {
            ordered.addAll(Arrays.asList((String[])parameters.get("fieldOrder")));
        }

        // are we done?
        if (ordered.size() == getFieldDescriptors().size())
        {
            return ordered;
        }

        // add those fields that we have missed to the end of the list.
        for (FieldDescriptor fd : getFieldDescriptors())
        {
            if (!ordered.contains(fd.getName()))
            {
                ordered.addLast(fd.getName());
            }
        }
        return ordered;
    }

}
