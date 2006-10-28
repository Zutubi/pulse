package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.ActionDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.FieldType;
import com.zutubi.pulse.form.ui.components.*;

import java.util.*;
import java.lang.reflect.Method;

/**
 * <class-comment/>
 */
public class FormFactory
{
    public Form createForm(FormDescriptor descriptor, Object instance)
    {
        List<String> fieldOrder = evaluateFieldOrder(descriptor);

        Form form = new Form();
        form.setId(instance.getClass().getSimpleName());
        form.setMethod("post");
        form.addParameters(descriptor.getParameters());

        int i = 1;
        for (String fieldName : fieldOrder)
        {
            UIComponent field = createField(descriptor.getFieldDescriptor(fieldName), instance);
            field.setTabindex(i++);
            form.addNestedComponent(field);
        }

        SubmitGroup submitGroup = new SubmitGroup();
        for (ActionDescriptor actionDescriptor : descriptor.getActionDescriptors())
        {
            Submit field = new Submit();
            field.setName(actionDescriptor.getAction());
            field.setValue(actionDescriptor.getAction() + ".label");
            field.setTabindex(i++);
            submitGroup.addNestedComponent(field);
        }

        form.addNestedComponent(submitGroup);

        return form;
    }

    protected List<String> evaluateFieldOrder(FormDescriptor descriptor)
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

    protected UIComponent createField(FieldDescriptor descriptor, Object instance)
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
        else if (FieldType.HIDDEN.equals(descriptor.getFieldType()))
        {
            return createHiddenField(descriptor);
        }
        else
        {
            throw new RuntimeException("Unsupported field type '" + descriptor.getFieldType() + "'");
        }
    }

    private UIComponent createCheckboxField(FieldDescriptor descriptor)
    {
        CheckboxField c = new CheckboxField();
        configureComponent(c, descriptor);

        return c;
    }

    private UIComponent createHiddenField(FieldDescriptor descriptor)
    {
        HiddenField c = new HiddenField();
        configureComponent(c, descriptor);
        return c;
    }

    private UIComponent createSelectField(FieldDescriptor descriptor, Object instance)
    {
        SelectField c = new SelectField();
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

    private UIComponent createRadioField(FieldDescriptor descriptor)
    {
        RadioField c = new RadioField();
        configureComponent(c, descriptor);
        return c;
    }

    private UIComponent createPasswordField(FieldDescriptor descriptor)
    {
        PasswordField c = new PasswordField();
        configureComponent(c, descriptor);
        return c;
    }

    private UIComponent createTextField(FieldDescriptor descriptor)
    {
        TextField c = new TextField();
        configureComponent(c, descriptor);
        return c;
    }

    private void configureComponent(UIComponent c, FieldDescriptor descriptor)
    {
        // default fields.
        c.setName(descriptor.getName());
        c.setId(descriptor.getName());
        c.setLabel(descriptor.getName() + ".label");
        c.setRequired(descriptor.isRequired());

        Map<String, Object> parameters = descriptor.getParameters();
        c.addParameters(parameters);
    }
}
