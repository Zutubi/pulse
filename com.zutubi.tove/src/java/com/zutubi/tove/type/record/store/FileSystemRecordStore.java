package com.zutubi.tove.type.record.store;

import com.zutubi.tove.transaction.Transaction;
import com.zutubi.tove.transaction.TransactionException;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.TransactionResource;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
    private static final String ACTION_IMPORT = "import";

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

    private FS fileSystem = new NativeFS();

    private File snapshotDirectory;
    private File backupSnapshotDirectory;
    private File newSnapshotDirectory;

    private File journalIndexFile;
    private File newJournalIndexFile;
    private File backupJournalIndexFile;

    private boolean stopRequested = false;

    private long compactionInterval = 60;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition lockCondition = lock.newCondition();

    public FileSystemRecordStore()
    {
    }

    /**
     * The interval (in seconds) between automatic compaction runs.
     *
     * @param interval interval in seconds.
     */
    public void setCompactionInterval(long interval)
    {
        this.compactionInterval = interval;
    }

    public void setFileSystem(FS fileSystem)
    {
        this.fileSystem = fileSystem;
    }

    public void initAndStartAutoCompaction() throws Exception
    {
        init();
        startAutoCompaction();
    }

    protected void startAutoCompaction()
    {
        stopRequested = false;
        Thread autoCompaction = new Thread(new Runnable()
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
        autoCompaction.setName("File System Record Store Compaction.");
        autoCompaction.start();
    }

    protected void stopAutoCompaction()
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
        if (!fileSystem.exists(persistenceDirectory) && !fileSystem.mkdirs(persistenceDirectory))
        {
            throw new IOException("Failed to create the record store persistence directory: " + persistenceDirectory.getAbsolutePath());
        }

        // initialise the file handles used by this record store.
        File initialised = new File(persistenceDirectory, ".initialised");

        snapshotDirectory = new File(persistenceDirectory, "snapshot");
        backupSnapshotDirectory = new File(persistenceDirectory, "snapshot.backup");
        newSnapshotDirectory = new File(persistenceDirectory, "snapshot.new");
        File snapshotJournalIdFile = new File(snapshotDirectory, "snapshot_id.txt");

        newJournalIndexFile = new File(persistenceDirectory, "index.new");
        journalIndexFile = new File(persistenceDirectory, "index");
        backupJournalIndexFile = new File(persistenceDirectory, "index.backup");

        // is this the first time we are initialising this directory?
        if (!fileSystem.exists(initialised))
        {
            if (!fileSystem.createNewFile(initialised))
            {
                throw new IOException("Failed to write to the record store persistence directory: " + persistenceDirectory.getAbsolutePath());
            }
        }
        else
        {
            if (fileSystem.exists(newSnapshotDirectory) || fileSystem.exists(snapshotDirectory) || fileSystem.exists(backupSnapshotDirectory))
            {
                recoverSnapshot(newSnapshotDirectory, snapshotDirectory, backupSnapshotDirectory);
            }
            if (fileSystem.exists(newJournalIndexFile) || fileSystem.exists(journalIndexFile) || fileSystem.exists(backupJournalIndexFile))
            {
                recoverIndex(newJournalIndexFile, journalIndexFile, backupJournalIndexFile);
            }
        }

        // load the snapshot if it exists.
        MutableRecord latestSnapshot = new MutableRecordImpl();
        if (fileSystem.exists(snapshotDirectory))
        {
            DefaultRecordSerialiser recordSerialiser = new DefaultRecordSerialiser(snapshotDirectory);
            latestSnapshot = recordSerialiser.deserialise();

            latestSnapshotId = Long.parseLong(IOUtils.fileToString(snapshotJournalIdFile));
        }

        inMemoryDelegate = new InMemoryRecordStore(latestSnapshot);
        inMemoryDelegate.setTransactionManager(transactionManager);
        inMemoryDelegate.init();

        // load the journal.

        if (journalIndexFile.isFile())
        {
            LOG.finest(Thread.currentThread().getId() + ": replay journal(start)");
            LOG.finest(Thread.currentThread().getId() + ":   snapshotid: (" + latestSnapshotId + ")");

            List<JournalEntry> journalEntries = readJournal();
            for (JournalEntry journalEntry : journalEntries)
            {
                if (lastCommittedJournalEntryId < journalEntry.getId())
                {
                    lastCommittedJournalEntryId = journalEntry.getId();
                }
            }

            if (latestSnapshotId < lastCommittedJournalEntryId)
            {
                for (JournalEntry journalEntry : journalEntries)
                {
                    if (latestSnapshotId < journalEntry.getId())
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
                        else if (journalEntry.getAction().equals(ACTION_IMPORT))
                        {
                            inMemoryDelegate.importRecords(journalEntry.getRecord());
                        }
                    }
                }
                compactNow();
            }
            LOG.finest(Thread.currentThread().getId() + ": replay journal(end)");
        }

        // At this stage, we have fully compacted content, so the next journal entry id will be the snapshot + 1.

        nextJournalEntryId = latestSnapshotId + 1;
    }

    private List<JournalEntry> readJournal() throws IOException
    {
        try
        {
            List<JournalEntry> journal = new LinkedList<JournalEntry>();
            if (fileSystem.exists(journalIndexFile))
            {
                BufferedReader reader = null;
                try
                {
                    reader = new BufferedReader(new FileReader(journalIndexFile));
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        long id = Long.valueOf(line);
                        String action = reader.readLine();
                        String path = reader.readLine();
                        long txnId = Long.valueOf(reader.readLine());

                        File recordDir = new File(persistenceDirectory, line);

                        JournalEntry journalEntry = new JournalEntry(action, path, recordDir);
                        journalEntry.setId(id);
                        journalEntry.setTxnId(txnId);
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
        catch (NumberFormatException e)
        {
            throw new IOException("Contents of the journal index are invalid. Reason: " + e.getMessage());
        }
    }

    //---( TransactionResource implementation )---
    public boolean prepare()
    {
        // prepare the journal entries.
        LOG.finest(Thread.currentThread().getId() + ": prepare(start)");

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

            if (fileSystem.exists(this.newJournalIndexFile) && !delete(this.newJournalIndexFile))
            {
                throw new IOException("Failed to delete " + this.newJournalIndexFile.getAbsolutePath());
            }

            if (!fileSystem.createNewFile(this.newJournalIndexFile))
            {
                throw new IOException("Failed to create " + this.newJournalIndexFile.getAbsolutePath());
            }

            writer = new FileWriter(this.newJournalIndexFile);

            long txnId = getCurrentTransactionId();

            for (JournalEntry entry : newJournalIndex)
            {
                writer.append(Long.toString(entry.getId()));
                writer.append('\n');
                writer.append(entry.getAction());
                writer.append('\n');
                writer.append(entry.getPath());
                writer.append('\n');
                writer.append(Long.toString(txnId));
                writer.append('\n');
            }

            // record the soon to be committed journal entries.
            for (JournalEntry entry : activeJournal)
            {
                File journalEntry = new File(persistenceDirectory, Long.toString(entry.getId()));
                if (fileSystem.exists(journalEntry) && !delete(journalEntry))
                {
                    throw new IOException("Failed to delete old journal entry: " + journalEntry.getAbsolutePath());
                }

                if (!writeRecord(journalEntry, entry.getRecord()))
                {
                    throw new IOException("Failed to write new journal entry: " + journalEntry.getAbsolutePath());
                }
            }

            LOG.finest(Thread.currentThread().getId() + ": prepare(end)");
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

    private long getCurrentTransactionId()
    {
        long txnId = -1;
        Transaction txn = transactionManager.getTransaction();
        if (txn != null)
        {
            txnId = txn.getId();
        }
        return txnId;
    }

    public synchronized void commit() throws TransactionException
    {
        LOG.finest(Thread.currentThread().getId() + ": commit(start)");

        // commit the journal entries.
        if (fileSystem.exists(newJournalIndexFile))
        {
            if (fileSystem.exists(backupJournalIndexFile) && !delete(backupJournalIndexFile))
            {
                throw new TransactionException("Failed to clean up stale backup.");
            }

            if (fileSystem.exists(journalIndexFile) && !fileSystem.renameTo(journalIndexFile, backupJournalIndexFile))
            {
                // This is a problem, any failures during the commit will leave things in an inconsistent state.
                // Will need to generate an exception that triggers a full refresh...
                throw new TransactionException("Failed to backup existing journal index.");
            }

            if (!fileSystem.renameTo(newJournalIndexFile, journalIndexFile))
            {
                throw new TransactionException("Failed to commit new journal index.");
            }

            if (fileSystem.exists(backupJournalIndexFile))
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

        LOG.finest(Thread.currentThread().getId() + ": commit(end)");
    }

    public void rollback() throws TransactionException
    {
        // rollback the journal entries
        LOG.finest(Thread.currentThread().getId() + ": rollback(start)");

        for (JournalEntry entry : activeJournal)
        {
            File journalEntry = new File(persistenceDirectory, Long.toString(entry.getId()));
            if (fileSystem.exists(journalEntry) && !delete(journalEntry))
            {
                // although unexpected, it doesnt matter too much.  A failure to delete will result
                // in a attempt to delete it at a later stage.
            }
        }

        recoverIndex(newJournalIndexFile, journalIndexFile, backupJournalIndexFile);

        activeJournal.clear();
        LOG.finest(Thread.currentThread().getId() + ": rollback(end)");
    }

    private void recoverIndex(File newIndex, File index, File backupIndex)
    {
        // attempt a recovery.
        if (fileSystem.exists(newIndex) && !delete(newIndex))
        {
            // problem, is it fatal? If we dont fail now, we will fail when we try to commit the next transaction..
            LOG.warning("Failed to delete uncommitted journal index during index recovery.");
        }

        if (fileSystem.exists(index))
        {
            // we have a committed index, cleanup the backup if it exists.
            if (fileSystem.exists(backupIndex))
            {
                delete(backupIndex);
            }
        }
        else
        {
            // rollback the backup.
            if (!fileSystem.exists(backupIndex))
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
                if (!fileSystem.renameTo(backupIndex, index))
                {
                    // problem, we failed to rollback to the index backup, not much we can do here.
                    throw new TransactionException("Failed to recover transaction, failed to rollback backup index.");
                }
            }
        }

    }

    public void insert(final String path, final Record record)
    {
        transactionManager.runInTransaction(new NullaryFunction()
        {
            public Object process()
            {
                JournalEntry journalEntry = new JournalEntry(ACTION_INSERT, path, record, nextJournalEntryId++);

                LOG.finest(Thread.currentThread().getId() + ": ("+journalEntry+")");
                activeJournal.add(journalEntry);
                inMemoryDelegate.insert(path, record);
                return null;
            }
        }, this);
    }

    public void update(final String path, final Record record)
    {
        transactionManager.runInTransaction(new NullaryFunction()
        {
            public Object process()
            {
                JournalEntry journalEntry = new JournalEntry(ACTION_UPDATE, path, record, nextJournalEntryId++);
                LOG.finest(Thread.currentThread().getId() + ": ("+journalEntry+")");
                activeJournal.add(journalEntry);
                inMemoryDelegate.update(path, record);
                return null;
            }
        }, this);
    }

    public Record delete(final String path)
    {
        return (Record) transactionManager.runInTransaction(new NullaryFunction()
        {
            public Object process()
            {
                JournalEntry journalEntry = new JournalEntry(ACTION_DELETE, path, nextJournalEntryId++);
                LOG.finest(Thread.currentThread().getId() + ": ("+journalEntry+")");
                activeJournal.add(journalEntry);
                return inMemoryDelegate.delete(path);
            }
        }, this);
    }

    public Record select()
    {
        return inMemoryDelegate.select();
    }

    public Record exportRecords()
    {
        return inMemoryDelegate.exportRecords();
    }

    public void importRecords(final Record record)
    {
        transactionManager.runInTransaction(new NullaryFunction()
        {
            public Object process()
            {
                activeJournal.add(new JournalEntry(ACTION_IMPORT, "", record, nextJournalEntryId++));
                inMemoryDelegate.importRecords(record);
                return null;
            }
        }, this);
    }

    public void setPersistenceDirectory(File persistentDir)
    {
        this.persistenceDirectory = persistentDir;
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    private boolean writeRecord(File file, Record record) throws IOException
    {
        if (fileSystem.exists(file))
        {
            // problems....
            throw new IOException("Can not write journal entry. File already exists. " + file.getAbsolutePath());
        }

        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        serialiser.serialise(file, record, true);

        return true;
    }

    private Record readRecord(File file)
    {
        XmlRecordSerialiser serialiser = new XmlRecordSerialiser();
        return serialiser.deserialise(file);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // if there are any problems during the compaction, we need to recover a stable state, undo what was done

    public void compactNow() throws IOException
    {
        LOG.finest(Thread.currentThread().getId() + ": compact(start)");

        try
        {
            // check if compact is required.
            if (lastCommittedJournalEntryId <= latestSnapshotId)
            {
                LOG.finest(Thread.currentThread().getId() + ": compact(end - not required)");
                return;
            }

            // what is the snapshot data to be persisted?  We synchronize on this so that we can ensure that
            // no commits occur when we are taking the data snapshot.
            Record newSnapshot;
            long snapshotJournalId;
            synchronized(this)
            {
                newSnapshot = inMemoryDelegate.select();

                // what journal entry id does this represent?
                snapshotJournalId = lastCommittedJournalEntryId;
            }

            // prepare the snapshot directory.
            if (fileSystem.exists(newSnapshotDirectory) && !delete(newSnapshotDirectory))
            {
                // problem preparing directories for snapshot dump
                throw new IOException("Failed to cleanup stale snapshot directory: " + newSnapshotDirectory.getAbsolutePath());
            }

            if (!fileSystem.mkdirs(newSnapshotDirectory))
            {
                // problem preparing directories for snapshot dump
                throw new IOException("Failed to create new snapshot directory: " + newSnapshotDirectory.getAbsolutePath());
            }

            DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(newSnapshotDirectory);
            serialiser.setMaxPathDepth(2);
            serialiser.serialise(newSnapshot, true);

            FileWriter writer = null;
            try
            {
                File newSnapshotJournalIdFile = new File(newSnapshotDirectory, "snapshot_id.txt");
                if (!fileSystem.createNewFile(newSnapshotJournalIdFile))
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

            if (fileSystem.exists(snapshotDirectory))
            {
                if (fileSystem.exists(backupSnapshotDirectory) && !delete(backupSnapshotDirectory))
                {
                    // problem clearing space for the backup
                    throw new IOException("Failed to clean up stale snapshot backup: " + backupSnapshotDirectory.getAbsolutePath());
                }
                if (!fileSystem.renameTo(snapshotDirectory, backupSnapshotDirectory))
                {
                    // problem with the backup...
                    throw new IOException("Failed to create snapshot backup: " + backupSnapshotDirectory.getAbsolutePath());
                }
            }

            if (!fileSystem.renameTo(newSnapshotDirectory, snapshotDirectory))
            {
                // problem with the commit. need to recover.
                throw new IOException("Failed to commit snapshot to " + snapshotDirectory.getAbsolutePath());
            }

            if (fileSystem.exists(backupSnapshotDirectory) && !delete(backupSnapshotDirectory))
            {
                // problem cleaning up backup. non fatal, but needs to be resolved before next commit else it will fail.
                LOG.severe("Failed to cleanup snapshop backup.");
            }

            latestSnapshotId = snapshotJournalId;

            LOG.finest(Thread.currentThread().getId() + ": compact(end)");
        }
        catch (IOException e)
        {
            try
            {
                recoverSnapshot(newSnapshotDirectory, snapshotDirectory, backupSnapshotDirectory);
            }
            catch (IOException e1)
            {
                // Just log this exception, do not override the original exception.
                LOG.severe(e1);
            }

            throw e;
        }

        cleanupJournalEntries();
    }

    private void recoverSnapshot(File newSnapshotDirectory, File snapshotDirectory, File backupSnapshotDirectory) throws IOException
    {
        // attempt a recovery.
        if (fileSystem.exists(newSnapshotDirectory) && !delete(newSnapshotDirectory))
        {
            throw new IOException("Failed to delete the new snapshot: " + newSnapshotDirectory.getAbsolutePath());
        }

        if (fileSystem.exists(snapshotDirectory))
        {
            // we have a committed snapshot directory, cleanup the backup if it exists.
            if (fileSystem.exists(backupSnapshotDirectory))
            {
                delete(backupSnapshotDirectory);
            }
        }
        else
        {
            // rollback the backup.
            if (!fileSystem.exists(backupSnapshotDirectory))
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
                if (!fileSystem.renameTo(backupSnapshotDirectory, snapshotDirectory))
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
                fileSystem.renameTo(journalEntry, deadFile);
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
        if (!fileSystem.exists(file))
        {
            return true;
        }

        boolean deleteSuccessful;
        if (file.isDirectory())
        {
            try
            {
                FileSystemUtils.rmdir(file);
                deleteSuccessful = true;
            }
            catch (IOException e)
            {
                deleteSuccessful = false;
            }
        }
        else
        {
            deleteSuccessful = fileSystem.delete(file);
        }

        if (!deleteSuccessful && !file.getName().endsWith(".dead"))
        {
            // rename...
            File deadFile = new File(file.getParentFile(), file.getName() + System.currentTimeMillis() + ".dead");
            deleteSuccessful = fileSystem.renameTo(file, deadFile);
        }

        return deleteSuccessful;
    }

    public class JournalEntry
    {
        private long id;
        private long txnId = -1;
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

        public File getRecordDir()
        {
            return recordDir;
        }

        long getId()
        {
            return id;
        }

        void setId(long id)
        {
            this.id = id;
        }

        public long getTxnId()
        {
            return txnId;
        }

        void setTxnId(long txnId)
        {
            this.txnId = txnId;
        }

        public String toString()
        {
            StringBuffer buffer = new StringBuffer(action).append(", ");
            buffer.append(id).append(", ").append(path).append(", ").append(record != null);
            return buffer.toString();
        }
    }
}
