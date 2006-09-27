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

    protected void evaluateExtraParameters()
    {
        super.evaluateExtraParameters();

        if (maxlength != null)
        {
            addParameter("maxlength", maxlength);
        }
        if (readonly != null)
        {
            addParameter("readonly", readonly);
        }
        if (size != null)
        {
            addParameter("size", size);
        }
    }

    public String getDefaultTemplate()
    {
        return TEMPLATE;
    }

    public void setMaxlength(String maxlength)
    {
        this.maxlength = maxlength;
    }

    public void setReadonly(String readonly)
    {
        this.readonly = readonly;
    }

    public void setSize(String size)
    {
        this.size = size;
    }
}
