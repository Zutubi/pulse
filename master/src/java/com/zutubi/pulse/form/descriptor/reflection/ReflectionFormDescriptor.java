package com.zutubi.pulse.form.descriptor.reflection;

import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.ActionDescriptor;
import com.zutubi.pulse.form.ui.components.FormComponent;
import com.zutubi.pulse.form.ui.components.SubmitComponent;
import com.zutubi.pulse.form.ui.components.SubmitGroupComponent;
import com.zutubi.pulse.form.ui.components.Component;

import java.util.*;

/**
 * <class-comment/>
 */
public class ReflectionFormDescriptor implements FormDescriptor
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
                new ReflectionActionDescriptor(ActionDescriptor.SAVE),
                new ReflectionActionDescriptor(ActionDescriptor.CANCEL),
                new ReflectionActionDescriptor(ActionDescriptor.RESET)
        );
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public FormComponent createForm()
    {
        List<String> fieldOrder = evaluateFieldOrder();

        FormComponent form = new FormComponent();
        form.setMethod("post");
        form.addParameters(getParameters());

        int i = 1;
        for (String fieldName : fieldOrder)
        {
            Component field = getFieldDescriptor(fieldName).createField();
            field.setTabIndex(i++);
            form.addNested(field);
        }

        SubmitGroupComponent submitGroup = new SubmitGroupComponent();
        for (ActionDescriptor actionDescriptor : getActionDescriptors())
        {
            SubmitComponent field = new SubmitComponent(actionDescriptor.getAction());
            field.setTabIndex(i++);
            submitGroup.addNested(field);
        }

        form.addNested(submitGroup);

        return form;
    }

    private List<String> evaluateFieldOrder()
    {
        // If a field order is defined, lets us it as the starting point.
        LinkedList<String> ordered = new LinkedList<String>();
        if (getFieldOrder() != null)
        {
            ordered.addAll(getFieldOrder());
        }

        // are we done?
        if (ordered.size() == fieldDescriptors.size())
        {
            return ordered;
        }

        // add those fields that we have missed to the end of the list.
        for (FieldDescriptor fd : fieldDescriptors)
        {
            if (!ordered.contains(fd.getName()))
            {
                ordered.addLast(fd.getName());
            }
        }

        return ordered;
    }
}
