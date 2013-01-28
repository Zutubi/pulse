package com.zutubi.pulse.master.tove.model;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.PulseActionMapper;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describes a form.  Can be instantiated with data (a record) to create the
 * form instance.
 */
public class FormDescriptor extends AbstractParameterised implements Descriptor
{
    public static final String PARAMETER_FIELD_ORDER = "fieldOrder";
    public static final String PARAMETER_SYMBOLIC_NAME = "symbolicName";

    private static final String DEFAULT_ACTION = "save";
    private static final String ACTION_NEXT = "next";
    private static final String ACTION_FINISH = "finish";

    private String name;
    private String id;
    private String action = DEFAULT_ACTION;
    private List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();
    private List<String> actions = new LinkedList<String>();
    private boolean readOnly = false;
    private boolean displayMode = false;

    /**
     * Indicates whether or not form submission should be via ajax.
     */
    private boolean ajax = false;

    /**
     * Defines url namespace in which the form submission should be made.
     */
    private String namespace = PulseActionMapper.ADMIN_NAMESPACE;

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

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public void add(FieldDescriptor descriptor)
    {
        fieldDescriptors.add(descriptor);
    }

    public FieldDescriptor getFieldDescriptor(final String name)
    {
        return find(fieldDescriptors, new Predicate<FieldDescriptor>()
        {
            public boolean apply(FieldDescriptor fieldDescriptor)
            {
                return fieldDescriptor.getName().equals(name);
            }
        }, null);
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

    public void setActions(String... actions)
    {
        this.actions.clear();
        this.actions.addAll(Arrays.asList(actions));
    }

    public void setActions(List<String> actions)
    {
        this.actions.clear();
        this.actions.addAll(actions);
    }

    public Form instantiate(String path, Record record)
    {
        Form form = new Form(name, id, ToveUtils.getConfigURL(path, action, null, namespace), getDefaultSubmit());
        form.setReadOnly(readOnly);
        form.setDisplayMode(displayMode);
        form.setAjax(ajax);
        form.setDefaultSubmit(getDefaultSubmit());
        form.addParameter("path", path);
        form.addAll(getParameters());
        
        List<String> fieldOrder = evaluateFieldOrder();
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
            form.add(field);
        }

        // add the submit fields.
        for (String submitAction : actions)
        {
            SubmitFieldDescriptor submitDescriptor = new SubmitFieldDescriptor(submitAction.equals(form.getDefaultSubmit()));
            submitDescriptor.setName(submitAction);
            Field submit = submitDescriptor.instantiate(path, record);
            form.add(submit);
        }

        return form;
    }

    private String getDefaultSubmit()
    {
        String defaultAction = actions.size() > 0 ? actions.get(0) : DEFAULT_ACTION;
        if (actions.contains(ACTION_NEXT))
        {
            defaultAction = ACTION_NEXT;
        }
        else if (actions.contains(ACTION_FINISH))
        {
            defaultAction = ACTION_FINISH;
        }

        return defaultAction;
    }

    protected List<String> evaluateFieldOrder()
    {
        // If a field order is defined, lets us it as the starting point.
        LinkedList<String> ordered = new LinkedList<String>();
        if (hasParameter(PARAMETER_FIELD_ORDER))
        {
            ordered.addAll(Arrays.asList((String[])getParameter(PARAMETER_FIELD_ORDER)));
        }

        return ToveUtils.evaluateFieldOrder(ordered, CollectionUtils.map(getFieldDescriptors(), new Mapping<FieldDescriptor, String>()
        {
            public String map(FieldDescriptor fieldDescriptor)
            {
                return fieldDescriptor.getName();
            }
        }));
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public void setDisplayMode(boolean displayMode)
    {
        this.displayMode = displayMode;
    }

    public void setAjax(boolean ajax)
    {
        this.ajax = ajax;
    }
}
