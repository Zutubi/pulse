package com.zutubi.pulse.upgrade.tasks;

/**
 * Holds information about a configuration scope which may be used within
 * upgrade tasks.
 */
public class ScopeDetails
{
    private String name;

    /**
     * @param name the name of the scope
     */
    public ScopeDetails(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
