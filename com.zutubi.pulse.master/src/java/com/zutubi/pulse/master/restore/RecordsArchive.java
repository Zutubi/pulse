/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.restore;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.XmlRecordSerialiser;
import com.zutubi.tove.type.record.store.RecordStore;

import java.io.File;
import java.io.IOException;

/**
 * The records archive handles the backup and restoration of a record store.
 */
public class RecordsArchive extends AbstractArchiveableComponent
{
    protected static final String ARCHIVE_FILENAME = "export.xml";

    private RecordStore recordStore;

    public String getName()
    {
        return "records";
    }

    public String getDescription()
    {
        return "Restores a snapshot of the configuration records (the editable project, agent " +
                "and server configuration).";
    }

    public void backup(File dir) throws ArchiveException
    {
        Record export = recordStore.exportRecords();

        File archive = getArchiveFile(dir);
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

    public boolean exists(File dir)
    {
        return getArchiveFile(dir).isFile();
    }

    public void restore(File dir)
    {
        File archive = getArchiveFile(dir);
        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        Record baseRecord = serialiser.deserialise(archive);

        recordStore.importRecords(baseRecord);
    }

    private File getArchiveFile(File dir)
    {
        return new File(dir, ARCHIVE_FILENAME);
    }

    public void setRecordStore(RecordStore recordStore)
    {
        this.recordStore = recordStore;
    }
}
