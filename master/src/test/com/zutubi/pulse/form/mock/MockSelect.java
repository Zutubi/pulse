package com.zutubi.pulse.form.mock;

import java.util.List;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class MockSelect
{
    private String field;

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public List<String> getFieldOptions()
    {
        return Arrays.asList("A", "B", "C");
    }
}
