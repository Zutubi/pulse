package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.TransactionResource;
import com.zutubi.prototype.transaction.TransactionException;
import com.zutubi.prototype.type.record.DefaultRecordSerialiser;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 */
public class FileSystemRecordStore implements RecordStore, TransactionResource
{
    private static final Logger LOG = Logger.getLogger(FileSystemRecordStore.class);

    private static final String ACTION_INSERT = "insert";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";

    /**
     * The in memory record store that will be used to hold an in memory version of the data this
     * persistent record store is managing.
     */
    private InMemoryRecordStore inMemoryDelegate = null;

    private TransactionManager transactionManager = null;

    /**
     * Each journal entry is given a unique id.  The next id to be used is held by this variable.
     */
    private long nextJournalEntryId = 1;

    private long lastCommittedJournalEntryId = 0;

    /**
     * The active journal entries are those journal entries associates with the current transaction.
     */
    private List<JournalEntry> activeJournal = new LinkedList<JournalEntry>();

    private List<JournalEntry> journal = new LinkedList<JournalEntry>();

    /**
     * The persistent record store directory.
     */
    private File persistenceDirectory;

    private long latestSnapshotId = 0;

    private File snapshotDirectory;
    private File backupSnapshotDirectory;
    private File newSnapshotDirectory;
    private File snapshotJournalIdFile;

    private File journalIndexFile;
    private File newJournalIndexFile;
    private File backupJournalIndexFile;

    private Thread autoCompaction;
    private boolean stopRequested = false;

    private long compactionInterval = 60;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition lockCondition = lock.newCondition();

    public FileSystemRecordStore()
    {
    }

    public void setCompactionInterval(long compactionInterval)
    {
        this.compactionInterval = compactionInterval;
    }

    public void initAndStartAutoCompaction() throws Exception
    {
        init();
        startAutoCompaction();
    }

    public void startAutoCompaction()
    {
        stopRequested = false;
        autoCompaction = new Thread(new Runnable()
        {
            public void run()
            {
                while (!stopRequested)
                {
                    lock.lock();

                    try
                    {
                        lockCondition.await(compactionInterval, TimeUnit.SECONDS);
                        if (!stopRequested)
                        {
                            compactNow();
                        }
                    }
                    catch (InterruptedException e)
                    {
                        // noop.
                    }
                    catch (IOException e)
                    {
                        LOG.severe(e);
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            }
        });
        autoCompaction.start();
    }

    public void stopAutoCompaction()
    {
        lock.lock();
        try
        {
            stopRequested = true;
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void init() throws Exception
    {
        if (!persistenceDirectory.exists() && !persistenceDirectory.mkdirs())
        {
            throw new IOException("Failed to create the record store persistence directory: " + persistenceDirectory.getAbsolutePath());
        }

        // initialise the file handles used by this record store.
        File initialised = new File(persistenceDirectory, ".initialised");

        snapshotDirectory = new File(persistenceDirectory, "snapshot");
        backupSnapshotDirectory = new File(persistenceDirectory, "snapshot.backup");
        newSnapshotDirectory = new File(persistenceDirectory, "snapshot.new");
        snapshotJournalIdFile = new File(snapshotDirectory, "snapshot_id.txt");

        newJournalIndexFile = new File(persistenceDirectory, "index.new");
        journalIndexFile = new File(persistenceDirectory, "index");
        backupJournalIndexFile = new File(persistenceDirectory, "index.backup");

        // is this the first time we are initialising this directory?
        if (!initialised.exists())
        {
            if (!initialised.createNewFile())
            {
                throw new IOException("Failed to write to the record store persistence directory: " + persistenceDirectory.getAbsolutePath());
            }
        }
        else
        {
            if (newSnapshotDirectory.exists() || snapshotDirectory.exists() || backupSnapshotDirectory.exists())
            {
                recoverSnapshot(newSnapshotDirectory, snapshotDirectory, backupSnapshotDirectory);
            }
            if (newJournalIndexFile.exists() || journalIndexFile.exists() || backupJournalIndexFile.exists())
            {
                recoverIndex(newJournalIndexFile, journalIndexFile, backupJournalIndexFile);
            }
        }

        MutableRecord latestSnapshot = new MutableRecordImpl();
        if (snapshotDirectory.exists())
        {
            DefaultRecordSerialiser recordSerialiser = new DefaultRecordSerialiser(snapshotDirectory);
            latestSnapshot = recordSerialiser.deserialise("");

            latestSnapshotId = Long.parseLong(IOUtils.fileToString(snapshotJournalIdFile));
        }

        inMemoryDelegate = new InMemoryRecordStore(latestSnapshot);
        inMemoryDelegate.setTransactionManager(transactionManager);

        // load the journal.

        boolean compactRequired = false;

        List<JournalEntry> journal = new LinkedList<JournalEntry>();
        if (journalIndexFile.isFile())
        {
            for (JournalEntry journalEntry : readJournal())
            {
                // remove those entries that are already part of the snapshot.
                if (latestSnapshotId < journalEntry.getId())
                {
                    journal.add(journalEntry);
                    compactRequired = true;

                    // the next journal entry id should be greater than those entries we have
                    // seen previously.
                    if (nextJournalEntryId <= journalEntry.getId())
                    {
                        nextJournalEntryId = journalEntry.getId() + 1;
                    }
                }
            }

            // apply journal entries to snapshot prior to compact.
            for (JournalEntry journalEntry : journal)
            {
                if (journalEntry.getAction().equals(ACTION_INSERT))
                {
                    inMemoryDelegate.insert(journalEntry.getPath(), journalEntry.getRecord());
                }
                else if (journalEntry.getAction().equals(ACTION_UPDATE))
                {
                    inMemoryDelegate.update(journalEntry.getPath(), journalEntry.getRecord());
                }
                else if (journalEntry.getAction().equals(ACTION_DELETE))
                {
                    inMemoryDelegate.delete(journalEntry.getPath());
                }
            }
        }

        // the last committed journal entry id is the id prior to the next journal entry.
        lastCommittedJournalEntryId = nextJournalEntryId - 1;

        // if we loaded any journal entries that were not already part of the snapshot, then compact.
        if (compactRequired)
        {
            compactNow();
        }

        // start up thread to trigger
    }

    private List<JournalEntry> readJournal() throws IOException
    {
        List<JournalEntry> journal = new LinkedList<JournalEntry>();
        if (journalIndexFile.exists())
        {
            BufferedReader reader = null;
            try
            {
                reader = new BufferedReader(new FileReader(journalIndexFile));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    int id = Integer.valueOf(line);

                    String action = reader.readLine();
                    String path = reader.readLine();
                    File recordDir = new File(persistenceDirectory, line);

                    //TODO: possible optimisation, delay the loading of the record until it is requested.
                    //      This is only an optimisation if we do not start 'caching' the journal.
                    // load the record.
//                    Record record = readRecord(recordDir);

                    JournalEntry journalEntry = new JournalEntry(action, path, recordDir);
                    journalEntry.setId(id);
                    journal.add(journalEntry);
                }
            }
            finally
            {
                IOUtils.close(reader);
            }
        }
        return journal;
    }

    //---( TransactionResource implementation )---
    public boolean prepare()
    {
        // prepare the journal entries.

        FileWriter writer = null;
        try
        {
            // a) remove those journal entries that are no longer needed due to a recent compact.
            // NOTE: if we need to roll back this commit, this change can remain.
            List<JournalEntry> cleanJournal = new LinkedList<JournalEntry>();
            for (JournalEntry entry : journal)
            {
                if (latestSnapshotId < entry.getId())
                {
                    cleanJournal.add(entry);
                }
            }
            journal = cleanJournal;

            // b) write new index file.

            // the new index file contains the existing journal entries and the new active journal entries.

            List<JournalEntry> newJournalIndex = new LinkedList<JournalEntry>();
            newJournalIndex.addAll(journal);
            newJournalIndex.addAll(activeJournal);

            // any problems here trigger a commit failure. This will result in a rollback, so we can handle the
            // necessary cleanup then.

            if (this.newJournalIndexFile.exists() && !delete(this.newJournalIndexFile))
            {
                throw new IOException("Failed to delete " + this.newJournalIndexFile.getAbsolutePath());
            }

            if (!this.newJournalIndexFile.createNewFile())
            {
                throw new IOException("Failed to create " + this.newJournalIndexFile.getAbsolutePath());
            }

            writer = new FileWriter(this.newJournalIndexFile);

            for (JournalEntry entry : newJournalIndex)
            {
                writer.append(Long.toString(entry.getId()));
                writer.append('\n');
                writer.append(entry.getAction());
                writer.append('\n');
                writer.append(entry.getPath());
                writer.append('\n');
            }

            // record the soon to be committed journal entries.
            for (JournalEntry entry : activeJournal)
            {
                File journalEntry = new File(persistenceDirectory, Long.toString(entry.getId()));
                if (journalEntry.exists() && !delete(journalEntry))
                {
                    throw new IOException("Failed to delete old journal entry: " + journalEntry.getAbsolutePath());
                }

                if (!writeRecord(journalEntry, entry.getRecord()))
                {
                    throw new IOException("Failed to write new journal entry: " + journalEntry.getAbsolutePath());
                }
            }

            return true;
        }
        catch (IOException e)
        {
            LOG.severe(e);
            return false;
        }
        finally
        {
            IOUtils.close(writer);
        }
    }

    public synchronized void commit() throws TransactionException
    {
        // commit the journal entries.
        if (newJournalIndexFile.exists())
        {
            if (backupJournalIndexFile.exists() && !delete(backupJournalIndexFile))
            {
                throw new TransactionException("Failed to clean up stale backup.");
            }

            if (journalIndexFile.exists() && !journalIndexFile.renameTo(backupJournalIndexFile))
            {
                // This is a problem, any failures during the commit will leave things in an inconsistent state.
                // Will need to generate an exception that triggers a full refresh...
                throw new TransactionException("Failed to backup existing journal index.");
            }

            if (!newJournalIndexFile.renameTo(journalIndexFile))
            {
                throw new TransactionException("Failed to commit new journal index.");
            }

            if (backupJournalIndexFile.exists())
            {
                delete(backupJournalIndexFile);
            }
        }

        // commit in memory state.
        if (activeJournal.size() > 0)
        {
            // being ultra defensive here to ensure transaction does not fail.  Get the last non-null entry from
            // the active journal. This should be the last entry...
            for (int i = activeJournal.size() - 1; 0 <= i; i--)
            {
                JournalEntry journalEntry = activeJournal.get(i);
                if (journalEntry != null)
                {
                    lastCommittedJournalEntryId = journalEntry.getId();
                    break;
                }
            }
        }

        journal.addAll(activeJournal);
        activeJournal.clear();
    }

    public void rollback() throws TransactionException
    {
        // rollback the journal entries

        for (JournalEntry entry : activeJournal)
        {
            File journalEntry = new File(persistenceDirectory, Long.toString(entry.getId()));
            if (journalEntry.exists() && !delete(journalEntry))
            {
                // although unexpected, it doesnt matter too much.  A failure to delete will result
                // in a attempt to delete it at a later stage.
            }
        }

        recoverIndex(newJournalIndexFile, journalIndexFile, backupJournalIndexFile);

        activeJournal.clear();
    }

    private void recoverIndex(File newIndex, File index, File backupIndex)
    {
        // attempt a recovery.
        if (newIndex.exists() && !delete(newIndex))
        {
            // problem, is it fatal? If we dont fail now, we will fail when we try to commit the next transaction..
            LOG.warning("Failed to delete uncommitted journal index during index recovery.");
        }

        if (index.exists())
        {
            // we have a committed index, cleanup the backup if it exists.
            if (backupIndex.exists())
            {
                delete(backupIndex);
            }
        }
        else
        {
            // rollback the backup.
            if (!backupIndex.exists())
            {
                // if the backup does not exist, then the original file did not exist either, so there is
                // nothing to do here.  No need to be overly paranoid.
/*
                // ok, this is bad. We should ALWAYS have this file. LOG the sad state and continue.
                throw new TransactionException("Failed to recover transaction, backup index does not exist.");
*/
            }
            else
            {
                if (!backupIndex.renameTo(index))
                {
                    // problem, we failed to rollback to the index backup, not much we can do here.
                    throw new TransactionException("Failed to recover transaction, failed to rollback backup index.");
                }
            }
        }

    }

    public Record insert(final String path, final Record record)
    {
        return execute(new Executable()
        {
            public Record execute()
            {
                activeJournal.add(new JournalEntry(ACTION_INSERT, path, record, nextJournalEntryId++));
                return inMemoryDelegate.insert(path, record);
            }
        });
    }

    public Record update(final String path, final Record record)
    {
        return execute(new Executable()
        {
            public Record execute()
            {
                activeJournal.add(new JournalEntry(ACTION_UPDATE, path, record, nextJournalEntryId++));
                return inMemoryDelegate.update(path, record);
            }
        });
    }

    public Record delete(final String path)
    {
        return execute(new Executable()
        {
            public Record execute()
            {
                activeJournal.add(new JournalEntry(ACTION_DELETE, path, nextJournalEntryId++));
                return inMemoryDelegate.delete(path);
            }
        });
    }

    public Record select()
    {
        return inMemoryDelegate.select();
    }

    public void setPersistenceDirectory(File persistentDir)
    {
        this.persistenceDirectory = persistentDir;
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    private static interface Executable
    {
        Record execute();
    }

    private Record execute(Executable action)
    {
        // ensure that we are part of the transaction.
        boolean activeTransaction = transactionManager.getTransaction() != null;
        if (activeTransaction)
        {
            transactionManager.getTransaction().enlistResource(this);
        }
        else
        {
            transactionManager.begin();
            transactionManager.getTransaction().enlistResource(this);
        }

        Record result = action.execute();

        if (!activeTransaction)
        {
            transactionManager.commit();
        }
        return result;
    }

    private boolean writeRecord(File dir, Record r) throws IOException
    {
        if (!dir.exists() && !dir.mkdirs())
        {
            // problems....
            throw new IOException();
        }

        if (r == null)
        {
            return true;
        }

        DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(dir);
        serialiser.serialise("", r, true);

        return true;
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

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // if there are any problems during the compaction, we need to recover a stable state, undo what was done

    public void compactNow() throws IOException
    {
        long oldSnapshotId = latestSnapshotId;

        try
        {
            // check if compact is required.
            if (lastCommittedJournalEntryId == latestSnapshotId)
            {
                return;
            }

            // what is the snapshot data to be persisted?  We synchronize on this so that we can ensure that
            // no commits occur when we are taking the data snapshot.
            Record newSnapshot = null;
            long snapshotJournalId = -1;
            synchronized(this)
            {
                newSnapshot = inMemoryDelegate.select();

                // what journal entry id does this represent?
                snapshotJournalId = lastCommittedJournalEntryId;
            }

            // prepare the snapshot directory.
            if (newSnapshotDirectory.exists() && !delete(newSnapshotDirectory))
            {
                // problem preparing directories for snapshot dump
                throw new IOException("Failed to cleanup stale snapshot directory: " + newSnapshotDirectory.getAbsolutePath());
            }

            if (!newSnapshotDirectory.mkdirs())
            {
                // problem preparing directories for snapshot dump
                throw new IOException("Failed to create new snapshot directory: " + newSnapshotDirectory.getAbsolutePath());
            }

            DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(newSnapshotDirectory);
            serialiser.serialise("", newSnapshot, true);

            FileWriter writer = null;
            try
            {
                File newSnapshotJournalIdFile = new File(newSnapshotDirectory, "snapshot_id.txt");
                if (!newSnapshotJournalIdFile.createNewFile())
                {
                    throw new IOException("Failed to create new snapshot id file: " + newSnapshotJournalIdFile.getAbsolutePath());
                }
                writer = new FileWriter(newSnapshotJournalIdFile);
                writer.append(Long.toString(snapshotJournalId));
                writer.flush();
            }
            finally
            {
                IOUtils.close(writer);
            }

            // now we commit these changes by swapping in this directory for the old one.

            if (snapshotDirectory.exists())
            {
                if (backupSnapshotDirectory.exists() && !delete(backupSnapshotDirectory))
                {
                    // problem clearing space for the backup
                    throw new IOException("Failed to clean up stale snapshot backup: " + backupSnapshotDirectory.getAbsolutePath());
                }
                if (!snapshotDirectory.renameTo(backupSnapshotDirectory))
                {
                    // problem with the backup...
                    throw new IOException("Failed to create snapshot backup: " + backupSnapshotDirectory.getAbsolutePath());
                }
            }

            if (!newSnapshotDirectory.renameTo(snapshotDirectory))
            {
                // problem with the commit. need to recover.
                throw new IOException("Failed to commit snapshot to " + snapshotDirectory.getAbsolutePath());
            }

            latestSnapshotId = snapshotJournalId;

            if (backupSnapshotDirectory.exists() && !delete(backupSnapshotDirectory))
            {
                // problem cleaning up backup. non fatal, but needs to be resolved before next commit else it will fail.
                LOG.severe("Failed to cleanup snapshop backup.");
            }

            cleanupJournalEntries();
        }
        catch (IOException e)
        {
            recoverSnapshot(newSnapshotDirectory, snapshotDirectory, backupSnapshotDirectory);

            latestSnapshotId = oldSnapshotId;

            throw e;
        }
    }

    private void recoverSnapshot(File newSnapshotDirectory, File snapshotDirectory, File backupSnapshotDirectory) throws IOException
    {
        // attempt a recovery.
        if (newSnapshotDirectory.exists() && !delete(newSnapshotDirectory))
        {
            // problem, but just log it and continue recovery. Do not override current exception.
            throw new IOException();
        }

        if (snapshotDirectory.exists())
        {
            // we have a committed snapshot directory, cleanup the backup if it exists.
            if (backupSnapshotDirectory.exists())
            {
                delete(backupSnapshotDirectory);
            }
        }
        else
        {
            // rollback the backup.
            if (!backupSnapshotDirectory.exists())
            {
                // if the backup does not exist, then the original file did not exist either, so there is
                // nothing to do here.  No need to be overly paranoid.
/*
                // ok, this is bad. We should ALWAYS have this directory. LOG the sad state and continue.
                throw new TransactionException("Failed to recover transaction, backup snapshot does not exist.");
*/
            }
            else
            {
                if (!backupSnapshotDirectory.renameTo(snapshotDirectory))
                {
                    // problem, we failed to rollback to the snapshot backup, not much we can do here.
                    // LOG this sad state of affairs.
                    throw new TransactionException("Failed to recover transaction, failed to rollback backup snapshot.");
                }
            }
        }
    }

    public void cleanupJournalEntries()
    {
        File[] journalEntries = persistenceDirectory.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                try
                {
                    Long.parseLong(pathname.getName());
                    return true;
                }
                catch (NumberFormatException e)
                {
                    return false;
                }
            }
        });

        // mark the journal entries as dead.
        for (File journalEntry : journalEntries)
        {
            long id = Long.parseLong(journalEntry.getName());
            if (id <= latestSnapshotId)
            {
                File deadFile = new File(journalEntry.getParentFile(), journalEntry.getName() + ".dead");
                journalEntry.renameTo(deadFile);
            }
        }

        // delete all of the dead files.
        for (File deadFile : persistenceDirectory.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".dead");
            }
        }))
        {
            if (!delete(deadFile))
            {
                LOG.warning("Failed to delete: " + deadFile.getAbsolutePath());
            }
        }
    }

    private boolean delete(File file)
    {
        if (!file.exists())
        {
            return true;
        }

        boolean deleteSuccessful = false;
        if (file.isDirectory())
        {
            deleteSuccessful = FileSystemUtils.rmdir(file);
        }
        else
        {
            deleteSuccessful = file.delete();
        }

        if (!deleteSuccessful && !file.getName().endsWith(".dead"))
        {
            // rename...
            File deadFile = new File(file.getParentFile(), file.getName() + System.currentTimeMillis() + ".dead");
            deleteSuccessful = file.renameTo(deadFile);
        }

        return deleteSuccessful;
    }

    public class JournalEntry
    {
        private long id;

        private String action;
        private String path;
        private Record record;
        private File recordDir;

        public JournalEntry(String action, String path)
        {
            this(action, path, (Record) null);
        }

        public JournalEntry(String action, String path, long id)
        {
            this(action, path, (Record) null, id);
        }

        public JournalEntry(String action, String path, Record record)
        {
            this(action, path, record, 0);
        }

        public JournalEntry(String action, String path, Record record, long id)
        {
            this.action = action;
            this.path = path;
            this.record = record;
            this.id = id;
        }

        public JournalEntry(String action, String path, File record)
        {
            this(action, path, record, 0);
        }

        public JournalEntry(String action, String path, File record, long id)
        {
            this.action = action;
            this.path = path;
            this.recordDir = record;
            this.id = id;
        }

        public String getAction()
        {
            return action;
        }

        public String getPath()
        {
            return path;
        }

        public Record getRecord()
        {
            if (record == null && !action.equals(ACTION_DELETE))
            {
                record = readRecord(recordDir);
            }
            return record;
        }

        long getId()
        {
            return id;
        }

        void setId(long id)
        {
            this.id = id;
        }
    }

}
