package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.master.model.SequenceEntry;

/**
 * Data access interface for the sequence entries.
 */
public interface SequenceEntryDao extends EntityDao<SequenceEntry>
{
    /**
     * Retrieve a sequence entry by name.
     *
     * @param name  the name of the sequence
     * @return  the sequence entry instance, or null if non is found.
     */
    SequenceEntry findByName(String name);
}
