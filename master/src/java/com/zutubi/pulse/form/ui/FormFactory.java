package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.ui.components.*;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.ActionDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.FieldType;
import com.zutubi.validation.bean.BeanUtils;
import com.zutubi.validation.bean.BeanException;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;

/**
 * <class-comment/>
 */
public class FormFactory
{
    public FormComponent createForm(FormDescriptor descriptor)
    {

        List<String> fieldOrder = evaluateFieldOrder(descriptor);

        FormComponent form = new FormComponent();
        form.setMethod("post");
        form.addParameters(descriptor.getParameters());

        int i = 1;
        for (String fieldName : fieldOrder)
        {
            Component field = createField(descriptor.getFieldDescriptor(fieldName));
            field.setTabIndex(i++);
            form.addNested(field);
        }

        SubmitGroupComponent submitGroup = new SubmitGroupComponent();
        for (ActionDescriptor actionDescriptor : descriptor.getActionDescriptors())
        {
            SubmitComponent field = new SubmitComponent(actionDescriptor.getAction());
            field.setTabIndex(i++);
            submitGroup.addNested(field);
        }

        form.addNested(submitGroup);

        return form;
    }

    private List<String> evaluateFieldOrder(FormDescriptor descriptor)
    {
        // If a field order is defined, lets us it as the starting point.
        LinkedList<String> ordered = new LinkedList<String>();
        if (descriptor.getFieldOrder() != null)
        {
            ordered.addAll(descriptor.getFieldOrder());
        }

        // are we done?
        if (ordered.size() == descriptor.getFieldDescriptors().size())
        {
            return ordered;
        }

        // add those fields that we have missed to the end of the list.
        for (FieldDescriptor fd : descriptor.getFieldDescriptors())
        {
            if (!ordered.contains(fd.getName()))
            {
                ordered.addLast(fd.getName());
            }
        }
        return ordered;
    }

    private Component createField(FieldDescriptor descriptor)
    {
        if (FieldType.TEXT.equals(descriptor.getFieldType()))
        {
            TextComponent c = new TextComponent();
            c.setName(descriptor.getName());
            c.setLabel(descriptor.getName() + ".label");
            c.setRequired(descriptor.isRequired());
            applyParameters(c, descriptor.getParameters());
            return c;
        }
        throw new RuntimeException("Unsupported field type '" + descriptor.getFieldType() + "'");
    }

    private void applyParameters(TextComponent c, Map<String, Object> parameters)
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
                BeanUtils.setProperty(entry.getKey(), entry.getValue(), c);
            }
            catch (BeanException e)
            {
                // noop.
            }
        }
    }
}
