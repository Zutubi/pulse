package com.zutubi.pulse.web.tree;

/**
 * <class-comment/>
 */
public class Listing
{
    protected String name;
    protected String type;
    
    protected String uid;

    public void setName(String name)
    {
        this.name = name;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getUid()
    {
        return uid;
    }
}
