package com.zutubi.pulse.master.restore;

import com.zutubi.tove.type.record.*;
import com.zutubi.tove.type.record.store.RecordStore;

import java.io.File;
import java.io.IOException;

/**
 * The records archive handles the backup and restoration of a record store.
 */
public class RecordsArchive extends AbstractArchiveableComponent
{
    private RecordStore recordStore;
    protected static final String ARCHIVE_FILENAME = "export.xml";

    public String getName()
    {
        return "records";
    }

    public String getDescription()
    {
        return "The records restoration will replace the current Pulse system configuration with the archived configuration.";
    }

    public void backup(File dir) throws ArchiveException
    {
        Record export = recordStore.exportRecords();

        File archive = new File(dir, ARCHIVE_FILENAME);
        try
        {
            if (!archive.createNewFile())
            {
                throw new ArchiveException("Failed to create: " + archive.getCanonicalPath());
            }
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }

        // serialise the record structure to disk.
        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        serialiser.serialise(archive, export, true);
    }

    public void restore(File dir)
    {
        File archive = new File(dir, ARCHIVE_FILENAME);
        if (!archive.isFile())
        {
            return;
        }

        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        Record baseRecord = serialiser.deserialise(archive);

        recordStore.importRecords(baseRecord);
    }

    public void setRecordStore(RecordStore recordStore)
    {
        this.recordStore = recordStore;
    }
}
