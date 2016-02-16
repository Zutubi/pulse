package com.zutubi.tove.ui.model.forms;

import com.zutubi.tove.annotations.FieldType;

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
}
