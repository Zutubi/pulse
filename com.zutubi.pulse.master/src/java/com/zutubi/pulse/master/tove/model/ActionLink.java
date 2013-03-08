package com.zutubi.pulse.master.tove.model;

import com.zutubi.util.Sort;

/**
 * A simple model for links (which may trigger JavaScript actions or be regular
 * hrefs).
 */
public class ActionLink implements Comparable<ActionLink>
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ActionLink that = (ActionLink) o;

        if (action != null ? !action.equals(that.action) : that.action != null)
        {
            return false;
        }
        if (argument != null ? !argument.equals(that.argument) : that.argument != null)
        {
            return false;
        }
        if (icon != null ? !icon.equals(that.icon) : that.icon != null)
        {
            return false;
        }
        if (label != null ? !label.equals(that.label) : that.label != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        result = 31 * result + (argument != null ? argument.hashCode() : 0);
        return result;
    }

    public int compareTo(ActionLink o)
    {
        Sort.StringComparator cmp = new Sort.StringComparator();
        return cmp.compare(getCompareString(), o.getCompareString());
    }
    
    private String getCompareString()
    {
        return String.valueOf(label) + String.valueOf(action) + String.valueOf(icon) + String.valueOf(argument);
    }
}
