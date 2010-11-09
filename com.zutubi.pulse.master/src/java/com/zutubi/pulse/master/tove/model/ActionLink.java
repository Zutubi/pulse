package com.zutubi.pulse.master.tove.model;

/**
 */
public class ActionLink
{
    private String action;
    private String label;
    private String icon;
    private String argument;

    public ActionLink(String action, String label, String icon)
    {
        this.action = action;
        this.label = label;
        this.icon = icon;
    }

    public ActionLink(String action, String label, String icon, String argument)
    {
        this.action = action;
        this.label = label;
        this.icon = icon;
        this.argument = argument;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public String getArgument()
    {
        return argument;
    }

    public void setArgument(String argument)
    {
        this.argument = argument;
    }
}
