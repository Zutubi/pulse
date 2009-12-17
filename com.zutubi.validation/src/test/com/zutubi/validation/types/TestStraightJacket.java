package com.zutubi.validation.types;

import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Regex;
import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public class TestStraightJacket
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
