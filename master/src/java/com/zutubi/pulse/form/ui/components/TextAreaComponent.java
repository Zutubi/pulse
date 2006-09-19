package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class TextAreaComponent extends FieldComponent
{
    public String getTemplateName()
    {
        return "textarea";
    }

    public void setCols(int cols)
    {
        addParameter("cols", cols);
    }

    public void setRows(int rows)
    {
        addParameter("rows", rows);
    }

    public void setWrap(boolean wrap)
    {
        addParameter("wrap", wrap);
    }

    public void setReadOnly(boolean readOnly)
    {
        if (readOnly)
        {
            addParameter("readonly", readOnly);
        }
        else
        {
            removeParameter("readonly");
        }
    }
}
