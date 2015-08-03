package com.zutubi.pulse.master.rest.model.forms;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class FormModel
{
    private static final String DEFAULT_ACTION = "save";
    private static final String ACTION_NEXT = "next";
    private static final String ACTION_FINISH = "finish";

    private String name;
    private String id;
    private String symbolicName;
    private String actionName = DEFAULT_ACTION;
    private List<String> actions = new ArrayList<>();
    private boolean readOnly = false;
    private boolean displayMode = false;

    private List<FieldModel> fields = new ArrayList<>();

    public FormModel(String name, String id, String symbolicName)
    {
        this.name = name;
        this.id = id;
        this.symbolicName = symbolicName;
    }

    public String getName()
    {
        return name;
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
}
