package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class TextArea extends UIComponent
{
    public String getDefaultTemplate()
    {
        return "textarea";
    }

    public void setCols(String cols)
    {
        addParameter("cols", cols);
    }

    public void setReadonly(String readonly)
    {
        addParameter("readonly", readonly);
    }

    public void setRows(String rows)
    {
        addParameter("rows", rows);
    }

    public void setWrap(String wrap)
    {
        addParameter("wrap", wrap);
    }
}
