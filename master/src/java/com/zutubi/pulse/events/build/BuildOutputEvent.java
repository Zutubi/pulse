package com.zutubi.pulse.events.build;

/**
 *
 *
 */
public class BuildOutputEvent extends BuildEvent implements OutputEvent
{
    private byte[] data;

    public BuildOutputEvent(Object source, byte[] data)
    {
        super(source, null, null);

        this.data = data;
    }

    public byte[] getData()
    {
        return data;
    }
}
