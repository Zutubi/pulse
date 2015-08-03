package com.zutubi.pulse.master.rest.model;

import com.zutubi.pulse.master.tove.model.ActionLink;

/**
 * Models an action that can be invoked on a configuration.  Note there may be multiple actions
 * with the same 'action', but they will have different 'argument' values for variants.
 *
 * FIXME kendo should replace ActionLink
 */
public class ActionModel
{
    private String action;
    private String label;
    private String icon;
    private String argument;

    public ActionModel(ActionLink actionLink)
    {
        this(actionLink.getAction(), actionLink.getLabel(), actionLink.getIcon(), actionLink.getArgument());
    }

    public ActionModel(String action, String label, String icon, String argument)
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

    public String getLabel()
    {
        return label;
    }

    public String getIcon()
    {
        return icon;
    }

    public String getArgument()
    {
        return argument;
    }
}
