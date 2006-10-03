package com.zutubi.pulse.form.descriptor;

import com.zutubi.pulse.form.descriptor.ActionDescriptor;

/**
 * <class-comment/>
 */
public class DefaultActionDescriptor implements ActionDescriptor
{
    private String action;

    public DefaultActionDescriptor(String action)
    {
        this.action = action;
    }

    public String getAction()
    {
        return action;
    }
}
