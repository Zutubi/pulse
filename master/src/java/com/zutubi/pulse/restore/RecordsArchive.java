package com.zutubi.pulse.restore;

import com.zutubi.prototype.type.record.RecordSerialiser;
import com.zutubi.prototype.type.record.DefaultRecordSerialiser;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordHandler;
import com.zutubi.prototype.type.record.store.RecordStore;

import java.io.File;

/**
 *
 *
 */
public class RecordsArchive extends AbstractArchivableComponent
{
    private RecordStore recordStore;

    public String getName()
    {
        return "records";
    }

    public String getDescription()
    {
        return "The records restoration will replace the current Pulse system configuration with " +
                "the archived configuration.";
    }

    public void backup(File base)
    {
        Record export = recordStore.exportRecords();

        RecordSerialiser serialiser = new DefaultRecordSerialiser(base);
        serialiser.serialise("", export, true);
    }

    public void restore(File base)
    {
        RecordSerialiser serialiser = new DefaultRecordSerialiser(base);
        Record baseRecord = serialiser.deserialise("", new RecordHandler()
        {
            public void handle(String path, Record record)
            {
                // use these callbacks to provide user feedback.
            }
        });

        recordStore.importRecords(baseRecord);
    }

    public void setRecordStore(RecordStore recordStore)
    {
        this.recordStore = recordStore;
    }
}
