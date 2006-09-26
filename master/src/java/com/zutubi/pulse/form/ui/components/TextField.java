package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class TextField extends UIComponent
{
    private static final String TEMPLATE = "text";

    protected String maxlength;
    protected String readonly;
    protected String size;

    public String getDefaultTemplate()
    {
        return TEMPLATE;
    }

    public void setMaxlength(String maxlength)
    {
        addParameter("maxlength", maxlength);
    }

    public void setReadonly(String readonly)
    {
        addParameter("readonly", readonly);
    }

    public void setSize(String size)
    {
        addParameter("size", size);
    }
}
