package com.zutubi.validation.mock;

import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Regex;
import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public class MockStraightJacket
{
    private String field;

    @Required @Email @Regex(pattern = ".")
    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }
}
