package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class TextArea extends UIComponent
{
    protected String cols;
    protected String readonly;
    protected String rows;
    protected String wrap;

    public String getDefaultTemplate()
    {
        return "textarea";
    }

    protected void evaluateExtraParameters()
    {
        super.evaluateExtraParameters();

        if (cols != null)
        {
            addParameter("cols", cols);
        }
        if (readonly != null)
        {
            addParameter("readonly", readonly);
        }
        if (rows != null)
        {
            addParameter("rows", rows);
        }
        if (wrap != null)
        {
            addParameter("wrap", wrap);
        }
    }

    public void setCols(String cols)
    {
        this.cols = cols;
    }

    public void setReadonly(String readonly)
    {
        this.readonly = readonly;
    }

    public void setRows(String rows)
    {
        this.rows = rows;
    }

    public void setWrap(String wrap)
    {
        this.wrap = wrap;
    }
}
