package com.zutubi.pulse.master.tove.model;

/**
 * A simple model for links (which may trigger JavaScript actions or be regular
 * hrefs).
 */
public class ActionLink
{
    private String action;
    private String label;
    private String icon;
    private String argument;

    /**
     * Creates a new link.
     *
     * @param action the action this link triggers, which may be symbolic
     *               (e.g. "trigger") or a full URL
     * @param label  the label to display as the link text
     * @param icon   name of the icon file to use, may be null (this name is
     *               converted to a full file path by client side rendering)
     */
    public ActionLink(String action, String label, String icon)
    {
        this(action, label, icon, null);
    }

    /**
     * Creates a new link with an argument to pass to the JavaScript handle
     * which it invokes.
     *
     * @param action   the action this link triggers, which should be symbolic
     *                 (e.g. "trigger") if argument is not null
     * @param label    the label to display as the link text
     * @param icon     name of the icon file to use, may be null (this name is
     *                 converted to a full file path by client side rendering)
     * @param argument argument to pass to the JavaScript handler invoked when
     *                 this link is clicked, may be null
     */
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
