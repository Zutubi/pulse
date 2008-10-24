package com.zutubi.pulse.master.tove.model;

import com.zutubi.pulse.master.tove.model.FieldDescriptor;

/**
 * Descriptor for textarea fields.
 */
public class TextAreaFieldDescriptor extends FieldDescriptor
{
    private static final String PARAMETER_COLS = "cols";
    private static final String PARAMETER_ROWS = "rows";
    private static final String PARAMETER_WRAP = "wrap";

    public static final String WRAP_OFF = "off";
    public static final String WRAP_PHYSICAL = "physical";
    public static final String WRAP_VIRTUAL = "virtual";

    public TextAreaFieldDescriptor()
    {
        setType("textarea");
        addParameter("submitOnEnter", false);
    }

    public int getCols()
    {
        return (Integer) getParameter(PARAMETER_COLS);
    }

    public void setCols(int cols)
    {
        addParameter(PARAMETER_COLS, cols);
    }

    public int getRows()
    {
        return (Integer) getParameter(PARAMETER_ROWS);
    }

    public void setRows(int rows)
    {
        addParameter(PARAMETER_ROWS, rows);
    }

    public String getWrap()
    {
        return (String) getParameter(PARAMETER_WRAP);
    }

    public void setWrap(String wrap)
    {
        addParameter(PARAMETER_WRAP, wrap);
    }
}
