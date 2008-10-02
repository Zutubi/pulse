package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

/**
 * Interface for objects that can locate records.  To obtain instances of
 * locators, see the {@link RecordLocators} class.
 */
public interface RecordLocator
{
    /**
     * Locate and return records of interest along with their paths.
     *
     * @param recordManager record manager from which the records are found
     * @return a mapping from path to record for all located records
     */
    Map<String, Record> locate(RecordManager recordManager);
}
