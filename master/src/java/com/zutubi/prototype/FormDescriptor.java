package com.zutubi.prototype;

import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.model.SubmitField;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 *
 *
 */
public class FormDescriptor implements Descriptor
{
    private static final String DEFAULT_ACTION = "save";

    private String id;
    private String action = DEFAULT_ACTION;
    private List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();
    private List<String> actions = new LinkedList<String>();
    private Map<String, Object> parameters = new HashMap<String, Object>();

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

    public void addParameter(String key, Object value)
    {
        this.parameters.put(key, value);
    }

    public void addAll(Map<String, Object> parameters)
    {
        this.parameters.putAll(parameters);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters)
    {
        this.parameters = parameters;
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
        form.setId(id);
        form.setAction(PrototypeUtils.getConfigURL(path, action, null));
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

        // add the submit fields.
        for (String action : actions)
        {
            form.add(new SubmitField(action).setTabindex(tabindex++));
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
