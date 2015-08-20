package com.zutubi.pulse.master.rest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Models an item of a templated collection, with child nodes.
 */
public class TemplateNodeModel
{
    private String name;
    private boolean concrete;
    private List<TemplateNodeModel> nested;

    public TemplateNodeModel(String name, boolean concrete)
    {
        this.name = name;
        this.concrete = concrete;
    }

    public String getName()
    {
        return name;
    }

    public boolean isConcrete()
    {
        return concrete;
    }

    public List<TemplateNodeModel> getNested()
    {
        return nested;
    }

    public void addChild(TemplateNodeModel child)
    {
        if (nested == null)
        {
            nested = new ArrayList<>();
        }

        nested.add(child);
    }
}
