package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.ui.components.*;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.ActionDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.FieldType;
import com.zutubi.validation.bean.BeanUtils;
import com.zutubi.validation.bean.BeanException;

import java.util.*;
import java.lang.reflect.Method;

/**
 * <class-comment/>
 */
public class FormFactory
{
    public FormComponent createForm(FormDescriptor descriptor, Object instance)
    {
        List<String> fieldOrder = evaluateFieldOrder(descriptor);

        FormComponent form = new FormComponent();
        form.setMethod("post");
        form.addParameters(descriptor.getParameters());

        int i = 1;
        for (String fieldName : fieldOrder)
        {
            Component field = createField(descriptor.getFieldDescriptor(fieldName), instance);
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
            ordered.addAll(Arrays.asList(descriptor.getFieldOrder()));
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

    private Component createField(FieldDescriptor descriptor, Object instance)
    {
        if (FieldType.TEXT.equals(descriptor.getFieldType()))
        {
            return createTextField(descriptor);
        }
        else if (FieldType.PASSWORD.equals(descriptor.getFieldType()))
        {
            return createPasswordField(descriptor);
        }
        else if (FieldType.RADIO.equals(descriptor.getFieldType()))
        {
            return createRadioField(descriptor);
        }
        else if (FieldType.SELECT.equals(descriptor.getFieldType()))
        {
            return createSelectField(descriptor, instance);
        }
        else if (FieldType.CHECKBOX.equals(descriptor.getFieldType()))
        {
            return createCheckboxField(descriptor);
        }
        else
        {
            throw new RuntimeException("Unsupported field type '" + descriptor.getFieldType() + "'");
        }
    }

    private Component createCheckboxField(FieldDescriptor descriptor)
    {
        CheckboxComponent c = new CheckboxComponent();
        configureComponent(c, descriptor);

        return c;
    }

    private Component createSelectField(FieldDescriptor descriptor, Object instance)
    {
        SelectComponent c = new SelectComponent();
        configureComponent(c, descriptor);

        // lookup the option list.
        String name = descriptor.getName();
        String optionMethodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1) + "Options";

        try
        {
            Method optionMethod = instance.getClass().getMethod(optionMethodName);
            Collection<String> list = (Collection<String>) optionMethod.invoke(instance);
            c.setList(list);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return c;
    }

    private Component createRadioField(FieldDescriptor descriptor)
    {
        RadioComponent c = new RadioComponent();
        configureComponent(c, descriptor);
        return c;
    }

    private Component createPasswordField(FieldDescriptor descriptor)
    {
        PasswordComponent c = new PasswordComponent();
        configureComponent(c, descriptor);
        return c;
    }

    private Component createTextField(FieldDescriptor descriptor)
    {
        TextComponent c = new TextComponent();
        configureComponent(c, descriptor);
        return c;
    }

    private void configureComponent(FieldComponent c, FieldDescriptor descriptor)
    {
        // default fields.
        c.setName(descriptor.getName());
        c.setLabel(descriptor.getName() + ".label");
        c.setRequired(descriptor.isRequired());

        Map<String, Object> parameters = descriptor.getParameters();

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
