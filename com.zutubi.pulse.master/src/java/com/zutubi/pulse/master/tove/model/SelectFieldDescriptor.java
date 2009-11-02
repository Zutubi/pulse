package com.zutubi.pulse.master.tove.model;

/**
 *
 *
 */
public class SelectFieldDescriptor extends OptionFieldDescriptor
{
    public static final String PARAMETER_LAZY = "lazy";

    public SelectFieldDescriptor()
    {
        setType("select");
        setLazy(false);
    }

    public boolean isLazy()
    {
        return getParameter(PARAMETER_LAZY, false);
    }

    public void setLazy(boolean lazy)
    {
        addParameter(PARAMETER_LAZY, lazy);
    }
}
