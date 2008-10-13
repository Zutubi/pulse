package com.zutubi.pulse.master.restore;

import com.zutubi.tove.type.record.*;
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
        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        serialiser.serialise(new File(base, "export.xml"), export, true);
    }

    public void restore(File base)
    {
        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        Record baseRecord = serialiser.deserialise(new File(base, "export.xml"));

        recordStore.importRecords(baseRecord);
    }

    public void setRecordStore(RecordStore recordStore)
    {
        this.recordStore = recordStore;
    }
}
