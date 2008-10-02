package com.zutubi.pulse.master.tove;

import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class Scope
{
    private final Scope parent;
    
    private final String name;

    protected Scope(String name)
    {
        this(null, name);
    }

    public Scope(Scope parent, String name)
    {
        this.parent = parent;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public Scope getParent()
    {
        return parent;
    }

    public List<String> getPath()
    {
        List<String> result = new LinkedList<String>();
        getPath(result);
        return result;
    }

    private void getPath(List<String> result)
    {
        if(parent != null)
        {
            parent.getPath(result);
        }

        result.add(name);
    }
}
