package com.zutubi.validation.mock;

import com.zutubi.validation.annotations.*;

/**
 * <class-comment/>
 */
public class MockStraightJacket
{
    private String field;

    @Required @Email @Name @Regex(pattern = ".")
    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }
}
