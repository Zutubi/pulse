package com.zutubi.pulse.core;

/**
 * A Dependency represents a dependency on an external resource. Dependencies are
 * checked prior to the execution of the recipe.
 *
 * Examples:
 *
 * the following dependency indicates that recipe requires java 1.5.
 *
 *     <dependency name="java" version="1.5"/>
 *
 * the following dependency indicates that the recipe needs to be executed on
 * a windows xp machine.
 *
 *     <dependency name="os" version="xp"/>
 *
 * the following dependency indicates that the recipe must be executed on the
 * host named 'builder'
 *
 *     <dependency name="host" version="builder"/>
 *
 */
public class Dependency
{
    private String name;
    private String version;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
