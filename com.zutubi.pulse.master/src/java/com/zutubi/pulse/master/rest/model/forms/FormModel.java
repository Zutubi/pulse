package com.zutubi.pulse.master.rest.model.forms;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a form to display in a UI. Forms are sets of fields, plus some config parameters.
 */
public class FormModel
{
    private static final String DEFAULT_ACTION = "save";
    private static final String ACTION_NEXT = "next";
    private static final String ACTION_FINISH = "finish";

    private String id;
    private String symbolicName;
    // FIXME kendo we are overloading the term "action" here for both the Webwork action and the form submit
    // actions. We should rename the latter once we've ditched the old forms to alleviate confusion.
    private String actionName = DEFAULT_ACTION;
    private List<String> actions = new ArrayList<>();
    private boolean readOnly = false;
    private boolean displayMode = false;

    private List<FieldModel> fields = new ArrayList<>();

    public FormModel(String id, String symbolicName)
    {
        this.id = id;
        this.symbolicName = symbolicName;
    }

    public String getId()
    {
        return id;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public String getActionName()
    {
        return actionName;
    }

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public List<String> getActions()
    {
        return actions;
    }

    public void setActions(List<String> actions)
    {
        this.actions = actions;
    }

    public String getDefaultSubmit()
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

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public boolean isDisplayMode()
    {
        return displayMode;
    }

    public void setDisplayMode(boolean displayMode)
    {
        this.displayMode = displayMode;
    }

    public List<FieldModel> getFields()
    {
        return fields;
    }

    public void addField(FieldModel field)
    {
        fields.add(field);
    }

    public void sortFields(List<String> fieldOrder)
    {
        List<FieldModel> sortedFields = new ArrayList<>(fields.size());
        for (final String fieldName: fieldOrder)
        {
            Optional<FieldModel> maybeField = Iterables.tryFind(fields, new Predicate<FieldModel>()
            {
                @Override
                public boolean apply(FieldModel input)
                {
                    return input.getName().equals(fieldName);
                }
            });

            if (maybeField.isPresent())
            {
                sortedFields.add(maybeField.get());
            }
        }

        fields = sortedFields;
    }
}
