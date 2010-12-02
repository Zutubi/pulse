package com.zutubi.pulse.master.tove.model;

import static com.zutubi.tove.annotations.FieldParameter.*;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.ConventionSupport;

import java.util.LinkedList;
import java.util.List;

/**
 * Describes a form field.
 */
public class FieldDescriptor extends AbstractParameterised implements Descriptor
{
    private FormDescriptor form;
    private String type;
    private String name;
    private Object value;

    public FieldDescriptor()
    {
        addParameter(ACTIONS, new LinkedList<String>());
        addParameter(SCRIPTS, new LinkedList<String>());
    }

    public Field instantiate(String path, Record instance)
    {
        // Implementation note:  The path parameter here is the same as the getParameters().get('path') value.
        // However, because it is the FieldDescriptor that is used in the FieldActionPredicate, the path needs
        // to be set before the field is instantiated.

        Field field = new Field(getType(), getName());
        field.setLabel(getName() + ConventionSupport.I18N_KEY_SUFFIX_LABEL);
        field.addAll(getParameters());

        // if we do not have a value set, then take the value from the instance.
        if (value != null)
        {
            field.setValue(value);
        }
        else if (instance != null)
        {
            field.setValue(instance.get(getName()));
        }

        return field;
    }

    public FormDescriptor getForm()
    {
        return form;
    }

    public void setForm(FormDescriptor form)
    {
        this.form = form;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public String getParentPath()
    {
        return (String) getParameter(PARENT_PATH);
    }

    public void setParentPath(String parentPath)
    {
        addParameter(PARENT_PATH, parentPath);
    }

    public String getPath()
    {
        return (String) getParameter(PATH);
    }

    public void setPath(String path)
    {
        addParameter(PATH, path);
    }

    public String getBaseName()
    {
        return (String) getParameter(BASE_NAME);
    }

    public void setBaseName(String baseName)
    {
        addParameter(BASE_NAME, baseName);
    }

    public TypeProperty getProperty()
    {
        return (TypeProperty) getParameter(PROPERTY);
    }

    public void setProperty(TypeProperty property)
    {
        addParameter(PROPERTY, property);
    }

    public boolean isRequired()
    {
        return getParameter(REQUIRED, false);
    }

    public void setRequired(boolean required)
    {
        addParameter(REQUIRED, required);
    }

    public boolean isConstrained()
    {
        return getParameter(CONSTRAINED, false);
    }

    public void setConstrained(boolean constrained)
    {
        addParameter(CONSTRAINED, constrained);
    }

    public boolean getSubmitOnEnter()
    {
        return getParameter(SUBMIT_ON_ENTER, false);
    }

    public void setSubmitOnEnter(boolean submitOnEnter)
    {
        addParameter(SUBMIT_ON_ENTER, submitOnEnter);
    }

    public void addAction(String template)
    {
        getActions().add(template);
    }

    public List<String> getActions()
    {
        return (List<String>) getParameter(ACTIONS);
    }

    public void addScript(String template)
    {
        getScripts().add(template);
    }

    public List<String> getScripts()
    {
        return (List<String>) getParameter(SCRIPTS);
    }
}
