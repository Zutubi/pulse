package com.zutubi.pulse.committransformers;

import java.util.List;
import java.util.Collections;

/**
 * <class comment/>
 */
public class SelectType
{
    private String type;

    private List<String> options;

    public SelectType(List<String> options)
    {
        Collections.sort(options);
        this.options = options;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public List<String> getTypeOptions()
    {
        return options;
    }
}
