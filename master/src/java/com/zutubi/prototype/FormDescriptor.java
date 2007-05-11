package com.zutubi.prototype;

import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.model.SubmitFieldDescriptor;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class FormDescriptor extends AbstractDescriptor
{
    private static final String DEFAULT_ACTION = "save";

    private String name;
    private String id;
    private String action = DEFAULT_ACTION;
    private List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();
    private List<String> actions = new LinkedList<String>();
    private boolean ajax = false;

//    private String[] fieldOrder;

    public void setName(String name)
    {
        this.name = name;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setAction(String action)
    {
        this.action = action;
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

    public List<String> getActions()
    {
        return Collections.unmodifiableList(actions);
    }

    public void setActions(List<String> actions)
    {
        this.actions.clear();
        this.actions.addAll(actions);
    }

    public Form instantiate(String path, Record record)
    {
        Form form = new Form();
        form.setName(name);
        form.setId(id);
        form.setAction(PrototypeUtils.getConfigURL(path, action, null, ajax));
        form.setAjax(ajax);
        form.addAll(getParameters());
        List<String> fieldOrder = evaluateFieldOrder();

        int tabindex = 1;
        for (String fieldName : fieldOrder)
        {
            FieldDescriptor fieldDescriptor = getFieldDescriptor(fieldName);
            if (fieldDescriptor == null)
            {
                // Ignore unknown fields. One awkward case where this happens is with the Check form.  Here, all of the
                // fields have the _check postfix, so any fields defined in the fieldOrder annotation will fail.
                continue;
            }
            
            Field field = fieldDescriptor.instantiate(path, record);
            field.setTabindex(tabindex++);
            form.add(field);
        }

        String defaultAction = getDefaultAction();

        // add the submit fields.
        for (String action : actions)
        {
            SubmitFieldDescriptor submitDescriptor = new SubmitFieldDescriptor(action.equals(defaultAction));
            submitDescriptor.setName(action);
            Field submit = submitDescriptor.instantiate(path, record);
            submit.setTabindex(tabindex++);
            form.add(submit);
        }

        return form;
    }

    private String getDefaultAction()
    {
        String defaultAction = "save";
        for(String action: actions)
        {
            if(action.equals("next"))
            {
                defaultAction = "next";
            }
            else if(action.equals("finish"))
            {
                // When next and finish are present, prefer next
                if(!defaultAction.equals("next"))
                {
                    defaultAction = "finish";
                }
            }
        }

        return defaultAction;
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

    public void setAjax(boolean ajax)
    {
        this.ajax = ajax;
    }
}
