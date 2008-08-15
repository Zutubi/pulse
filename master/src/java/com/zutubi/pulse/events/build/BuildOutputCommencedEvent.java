package com.zutubi.pulse.events.build;

/**
 *
 *
 */
public class BuildOutputCommencedEvent extends BuildEvent
{
    private String name;

    public BuildOutputCommencedEvent(Object source, String name)
    {
        super(source, null, null);
        this.name = name;
    }

    public String getCommandName()
    {
        return this.name;
    }
}
