package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.type.record.Record;

/**
 * A larger text entry field with multiple rows.
 */
public class TextAreaFieldModel extends FieldModel
{
    private static final int APPROX_CHARS_PER_ROW = 60;
    private static final int MAX_ROWS = 10;

    public boolean autoSize;
    public int cols;
    public int rows;
    public String wrap;

    public TextAreaFieldModel()
    {
        setType(FieldType.TEXTAREA);
        setSubmitOnEnter(true);
    }

    public boolean isAutoSize()
    {
        return autoSize;
    }

    public void setAutoSize(boolean autoSize)
    {
        this.autoSize = autoSize;
    }

    public int getCols()
    {
        return cols;
    }

    public void setCols(int cols)
    {
        this.cols = cols;
    }

    public int getRows()
    {
        return rows;
    }

    public void setRows(int rows)
    {
        this.rows = rows;
    }

    public String getWrap()
    {
        return wrap;
    }

    public void setWrap(String wrap)
    {
        this.wrap = wrap;
    }

    // FIXME kendo this is never called, should be done client side?
    public void instantiate(String path, Record instance)
    {
        if (isAutoSize())
        {
            Object value = new Object();//field.getValue();
            if (value != null && value instanceof String)
            {
                int rows = 1;
                int length = ((String) value).length();
                if (length > APPROX_CHARS_PER_ROW)
                {
                    rows = Math.min(length / APPROX_CHARS_PER_ROW + 1, MAX_ROWS);
                }

                setRows(rows);
            }
        }
    }
}
