package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.events.OutputEvent;

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
