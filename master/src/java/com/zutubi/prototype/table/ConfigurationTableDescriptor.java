package com.zutubi.prototype.table;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class ConfigurationTableDescriptor extends TableDescriptor
{
    private List<String> actions = new LinkedList<String>();

    public List<String> getActions()
    {
        return actions;
    }

    public void addAction(String action)
    {
        actions.add(action);
    }
}
