package com.zutubi.pulse.master.model;

/**
 * A sequence provides an always increasing sequence of numbers.
 */
public interface Sequence
{
    /**
     * Get the next value in the sequence.
     *
     * @return next value.
     */
    long getNext();
}
