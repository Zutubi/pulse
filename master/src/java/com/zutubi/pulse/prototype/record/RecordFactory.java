package com.zutubi.pulse.prototype.record;

/**
 * A simple interface for the creation of new record instances.  Allows the
 * type of instance to be controlled when loading via the RecordManager.
 *
 * @see RecordManager
 * @deprecated
 */
public interface RecordFactory
{
    Record create(String symbolicName);
}
