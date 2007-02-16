package com.zutubi.prototype.model;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class Form
{
    private List<Field> fields = new LinkedList<Field>();

    private List<String> actions = new LinkedList<String>();

    private String id;

    private String action = "config.action";

    public Form()
    {
        actions.add("save");
        actions.add("cancel");
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public void add(Field field)
    {
        this.fields.add(field);
    }

    public List<Field> getFields()
    {
        return fields;
    }

    public void setFields(List<Field> fields)
    {
        this.fields = fields;
    }

    public void addAction(String action)
    {
        this.actions.add(action);
    }
    
    public List<String> getActions()
    {
        return actions;
    }

    public void setActions(List<String> actions)
    {
        this.actions = actions;
    }
}
