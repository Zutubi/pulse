package com.zutubi.pulse.restore;

import com.zutubi.tove.type.record.DefaultRecordSerialiser;
import com.zutubi.tove.type.record.NoopRecordHandler;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordSerialiser;
import com.zutubi.tove.type.record.store.RecordStore;

import java.io.File;

/**
 * The records archive handles the backup and restoration of a record store.
 *
 */
public class RecordsArchive extends AbstractArchiveableComponent
{
    private RecordStore recordStore;

    public String getName()
    {
        return "records";
    }

    public String getDescription()
    {
        return "The records restoration will replace the current Pulse system configuration with the archived configuration.";
    }

    public void backup(File base)
    {
        Record export = recordStore.exportRecords();

        // serialise the record structure to disk.
        RecordSerialiser serialiser = new DefaultRecordSerialiser(base);
        serialiser.serialise("", export, true);
    }

    public void restore(File base)
    {
        RecordSerialiser serialiser = new DefaultRecordSerialiser(base);
        Record baseRecord = serialiser.deserialise("", new NoopRecordHandler());

        recordStore.importRecords(baseRecord);
    }

    public void setRecordStore(RecordStore recordStore)
    {
        this.recordStore = recordStore;
    }
}
