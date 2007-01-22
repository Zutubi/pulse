package com.zutubi.pulse.form.ui.components;

/**
 *
 */
public class Column extends UIComponent
{
    private static final String OPEN_TEMPLATE = "columnheader";
    private static final String TEMPLATE = "column";

    public Column(String name)
    {
        this.name = name;
    }

    public String getDefaultHeaderTemplate()
    {
        return OPEN_TEMPLATE;
    }

    public String getDefaultTemplate()
    {
        return TEMPLATE;
    }
}
