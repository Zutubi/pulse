package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.model.Sequence;

/**
 * Base implementation of the schedule handler that manages the allocation
 * of the unique handler id.
 */
public abstract class BaseMetaBuildHandler implements MetaBuildHandler
{
    private Sequence sequence;

    private long metaBuildId;

    public BaseMetaBuildHandler()
    {
    }

    public void init()
    {
        metaBuildId = sequence.getNext();
    }

    public Long getMetaBuildId()
    {
        return metaBuildId;
    }

    public void setSequence(Sequence sequence)
    {
        this.sequence = sequence;
    }
}
