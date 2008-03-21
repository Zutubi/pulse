package com.zutubi.prototype.model;

/**
 */
public class ActionLink
{
    private String action;
    private String label;
    private String icon;

    public ActionLink(String action, String label, String icon)
    {
        this.action = action;
        this.label = label;
        this.icon = icon;
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
}
