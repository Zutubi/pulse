package com.zutubi.prototype.model;

/**
 *
 *
 */
public class Column extends UIComponent
{
    private static final String PARAM_LINK = "link";

    public Column()
    {
        setSpan(1); // default span.
    }

    public void setName(String name)
    {
        parameters.put("name", name);
    }

    public String getName()
    {
        return (String) parameters.get("name");
    }

    public int getSpan()
    {
        return (Integer)parameters.get("span");
    }

    public void setSpan(int span)
    {
        parameters.put("span", span);
    }

    public Object getValue()
    {
        return parameters.get("value");
    }

    public void setValue(Object value)
    {
        parameters.put("value", value);
    }

    public String getLink()
    {
        return (String) parameters.get(PARAM_LINK);
    }

    public void setLink(String link)
    {
        parameters.put(PARAM_LINK, link);
    }
}
