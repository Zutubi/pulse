package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.type.record.Record;

/**
 * Descriptor for textarea fields.
 */
public class TextAreaFieldDescriptor extends FieldDescriptor
{
    private static final String PARAMETER_AUTO_SIZE = "autoSize";
    private static final String PARAMETER_COLS = "cols";
    private static final String PARAMETER_ROWS = "rows";
    private static final String PARAMETER_WRAP = "wrap";

    private static final int APPROX_CHARS_PER_ROW = 60;
    private static final int MAX_ROWS = 10;

    public TextAreaFieldDescriptor()
    {
        setType("textarea");
        addParameter("submitOnEnter", false);
    }

    public boolean getAutoSize()
    {
        return getParameter(PARAMETER_AUTO_SIZE, false);
    }

    public void setAutoSize(boolean autoSize)
    {
        addParameter(PARAMETER_AUTO_SIZE, autoSize);
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

    @Override
    public Field instantiate(String path, Record instance)
    {
        Field field = super.instantiate(path, instance);
        if (getAutoSize())
        {
            Object value = field.getValue();
            if (value != null && value instanceof String)
            {
                int rows = 1;
                int length = ((String) value).length();
                if (length > APPROX_CHARS_PER_ROW)
                {
                    rows = Math.min(length / APPROX_CHARS_PER_ROW + 1, MAX_ROWS);
                }

                field.getParameters().put(PARAMETER_ROWS, rows);
            }
        }

        return field;
    }
}
