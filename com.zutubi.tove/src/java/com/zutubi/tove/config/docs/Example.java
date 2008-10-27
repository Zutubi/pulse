package com.zutubi.tove.config.docs;

/**
 * An example configuration for a field.
 */
public class Example
{
    private String value;
    private String blurb;

    public Example(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getBlurb()
    {
        return blurb;
    }

    public void setBlurb(String blurb)
    {
        this.blurb = blurb;
    }
}
