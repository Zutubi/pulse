package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * An entry in the sequence table, storing the name and next available value of the sequence.
 */
public class SequenceEntry extends Entity
{
    private String name;
    private long next;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getNext()
    {
        return next;
    }

    public void setNext(long next)
    {
        this.next = next;
    }
}
