package com.zutubi.pulse.master.model;

/**
 * The sequence manager provides Pulse with access to a sequence of numbers.
 * These sequences are characterised by a) not repeating and b) always ascending.
 */
public interface SequenceManager
{
    /**
     * Get the sequence uniquely identified by the specified name.
     *
     * @param name  the name of the sequence to be retrieved.
     * @return the sequence.
     */
    Sequence getSequence(String name);
}
