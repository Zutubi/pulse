package com.zutubi.pulse.form.descriptor.reflection;

import com.zutubi.pulse.form.descriptor.ActionDescriptor;

/**
 * <class-comment/>
 */
public class ReflectionActionDescriptor implements ActionDescriptor
{
    private String action;

    public ReflectionActionDescriptor(String action)
    {
        this.action = action;
    }

    public String getAction()
    {
        return action;
    }
}
