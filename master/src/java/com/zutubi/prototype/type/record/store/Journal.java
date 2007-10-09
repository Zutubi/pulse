package com.zutubi.prototype.type.record.store;

import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.DefaultRecordSerialiser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileFilter;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

/**
 * The journal maintains a series of journal entries.  These entries contains action:path:record
 * details corresponding to actions that change the state of the file system record store.  By storing
 * the details of incremental changes made to the file system record store, the full system state does
 * not need to be re-writted after every commit.
 *
 * When a change is made to the file system record store, the details of that change are recorded
 * in this journal.  These changes can later be retrieved and reapplied to the file system record
 * store.
 *
 *
 */
public class Journal
{
    private static final Logger LOG = Logger.getLogger(Journal.class);

    /**
     * The base directory in which the journal is stored.
     */
    private File base;

    /**
     * Each journal entry is given a unique id.  The next id to be used is held by this variable.
     */
    private int nextId;

    /**
     * The list of journal entries.
     */
    private List<JournalEntry> journalEntries;

    private boolean clearRequested = false;

    public Journal(File base)
    {
        validateBaseDirectoryArg(base);
        
        this.base = base;

        recover();
    }

    private void validateBaseDirectoryArg(File base) throws IllegalArgumentException
    {
        if (base == null)
        {
            throw new IllegalArgumentException("The base journal directory argument is invalid.  It is null.");
        }
        if (!base.exists())
        {
            throw new IllegalArgumentException("The base journal directory ("+base.getAbsolutePath()+") is invalid.  It does not exist.");
        }
        if (!base.isDirectory())
        {
            throw new IllegalArgumentException("The base journal directory ("+base.getAbsolutePath()+") is invalid.  It is not a directory.");
        }
    }

    public void add(JournalEntry newEntry)
    {
        if (clearRequested)
        {
            throw new IllegalStateException("Journal is being cleared.  No new entries may be added until it is committed/rolledback.");
        }
        allocateEntryId(newEntry);
        journalEntries.add(newEntry);
    }

    private void allocateEntryId(JournalEntry newEntry)
    {
        newEntry.setId(nextId);
        nextId++;
    }

    private void writeIndexFile(File indexFile) throws IOException
    {
        FileWriter writer = null;
        try
        {
            indexFile.createNewFile();
            writer = new FileWriter(indexFile);
            for (JournalEntry entry : journalEntries)
            {
                writer.append(Integer.toString(entry.getId()));
                writer.append('\n');
                writer.append(entry.getAction());
                writer.append('\n');
                writer.append(entry.getPath());
                writer.append('\n');

                // check if we need to persist the record details.
                File recordDir = new File(base, Integer.toString(entry.getId()));
                if (!recordDir.isDirectory())
                {
                    writeRecord(recordDir, entry.getRecord());
                }
            }
        }
        finally
        {
            IOUtils.close(writer);
        }
    }

    public List<JournalEntry> getEntries()
    {
        return journalEntries;
    }

    public int size()
    {
        return journalEntries.size();
    }

    public JournalEntry get(int index)
    {
        return journalEntries.get(index);
    }

    public void clear()
    {
        journalEntries.clear();
        clearRequested = true;
    }

    public boolean prepare()
    {
        try
        {
            writeIndexFile(new File(base, "index.new"));
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public void commit()
    {
        File indexFile = new File(base, "index");
        File newIndexFile = new File(base, "index.new");
        File indexBackupFile = new File(base, "index.backup");

        if (newIndexFile.isFile())
        {
            if (indexFile.isFile())
            {
                indexFile.renameTo(indexBackupFile);
            }
            newIndexFile.renameTo(indexFile);
        }

        if (indexBackupFile.isFile())
        {
            indexBackupFile.delete();
        }

        // clean up record directories.
        if (clearRequested)
        {
            cleanupRecordDirectories();
            nextId = 1;
            clearRequested = false;
        }
    }

    public void rollback()
    {
        recover();
    }

    private void recover()
    {
        // clean up index files.
        File indexFile = new File(base, "index");
        File newIndexFile = new File(base, "index.new");
        File indexBackupFile = new File(base, "index.backup");

        if (newIndexFile.exists())
        {
            newIndexFile.delete();
        }

        if (indexFile.exists())
        {
            if (indexBackupFile.exists())
            {
                indexBackupFile.delete();
            }
        }
        else
        {
            if (indexBackupFile.exists())
            {
                indexBackupFile.renameTo(indexFile);
            }
        }

        // load contents of index file.
        journalEntries = new LinkedList<JournalEntry>();
        int maxId = 0;
        Set<String> knownIds = new HashSet<String>();
        if (indexFile.isFile())
        {
            BufferedReader reader = null;
            try
            {
                reader = new BufferedReader(new FileReader(indexFile));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    int id = Integer.valueOf(line);
                    if (id > maxId)
                    {
                        maxId = id;
                    }
                    knownIds.add(line);

                    String action = reader.readLine();
                    String path = reader.readLine();
                    File recordDir = new File(base, line);

                    // load the record.
                    Record record = readRecord(recordDir);

                    JournalEntry journalEntry = new JournalEntry(action, path, record);
                    journalEntry.setId(id);
                    journalEntries.add(journalEntry);
                }
            }
            catch (FileNotFoundException e)
            {
                // not going to happen.  We have already checked for the existance of the file.
            }
            catch (IOException e)
            {
                LOG.warning(e);
            }
            finally
            {
                IOUtils.close(reader);
            }
        }

        // initialise the next id.
        nextId = maxId + 1;

        // dont forget to cleanup all those dirs that should not be there.
        cleanupRecordDirectories(knownIds);

        clearRequested = false;
    }

    private void cleanupRecordDirectories()
    {
        Set<String> knownIds = new HashSet<String>();
        for (JournalEntry entry : journalEntries)
        {
            knownIds.add(Integer.toString(entry.getId()));
        }
        cleanupRecordDirectories(knownIds);
    }

    private void cleanupRecordDirectories(Set<String> knownIds)
    {
        File[] directories = base.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        });
        for (File dir : directories)
        {
            if (!knownIds.contains(dir.getName()))
            {
                FileSystemUtils.rmdir(dir);
            }
        }
    }

    private void writeRecord(File dir, Record r)
    {
        if (r != null)
        {
            DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(dir);
            serialiser.serialise("", r, true);
        }
    }

    private Record readRecord(File dir)
    {
        if (dir.isDirectory())
        {
            DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(dir);
            return serialiser.deserialise("");
        }
        return null;
    }
}
