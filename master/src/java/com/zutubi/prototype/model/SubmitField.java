package com.zutubi.prototype.model;

/**
 *
 *
 */
public class SubmitField extends Field
{
    public SubmitField()
    {
        setType("submit");
    }

    public SubmitField(String name)
    {
        setName(name);
        setValue(name);
        setType("submit");
    }
}
