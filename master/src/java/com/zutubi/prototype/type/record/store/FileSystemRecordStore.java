package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.TransactionResource;
import com.zutubi.prototype.type.record.DefaultRecordSerialiser;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class FileSystemRecordStore implements RecordStore, TransactionResource
{
    private static final String BACKUP_DIR = ".backup";
    private static final String ACTIVE_TXN_DIR = ".active";
    private static final String PERSISTENT_DIR = "data";
    private static final String JOURNAL_DIR = ".journal";

    private File persistenceDir;

    private InMemoryRecordStore inMemoryStore;

    private TransactionManager transactionManager;

    private Journal journal;

    public FileSystemRecordStore()
    {
    }

    public void setPersistenceDir(File persistenceDir)
    {
        this.persistenceDir = persistenceDir;
    }

    public void init()
    {
        recover();

        // load.
        File data = new File(persistenceDir, PERSISTENT_DIR);
        DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(data);
        MutableRecord base = serialiser.deserialise("");

        inMemoryStore = new InMemoryRecordStore(base);
        inMemoryStore.setTransactionManager(transactionManager);

        File journalDir = new File(persistenceDir, JOURNAL_DIR);
        journalDir.mkdirs();
        journal = new Journal(journalDir);

        // apply existing journal entries.
        for (JournalEntry entry : journal.getEntries())
        {
            if (entry.getAction().equals("insert"))
            {
                inMemoryStore.insert(entry.getPath(), entry.getRecord());
            }
            else if (entry.getAction().equals("update"))
            {
                inMemoryStore.update(entry.getPath(), entry.getRecord());
            }
            else if (entry.getAction().equals("delete"))
            {
                inMemoryStore.delete(entry.getPath());
            }
        }
    }

    public Record insert(final String path, final Record record)
    {
        return execute(new Executable()
        {
            public Record execute(RecordStore base)
            {
                return insert(base, path, record);
            }
        });
    }

    public Record update(final String path, final Record record)
    {
        return execute(new Executable()
        {
            public Record execute(RecordStore base)
            {
                return update(base, path, record);
            }
        });
    }

    public Record delete(final String path)
    {
        return execute(new Executable()
        {
            public Record execute(RecordStore base)
            {
                return delete(base, path);
            }
        });
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

        Record result = action.execute(inMemoryStore);

        if (!activeTransaction)
        {
            transactionManager.commit();
        }
        return result;
    }

    private Record insert(RecordStore base, String path, Record record)
    {
        journal.add(new JournalEntry("insert", path, record));
        return base.insert(path, record);
    }

    private Record update(RecordStore base, String path, Record record)
    {
        journal.add(new JournalEntry("update", path, record));
        return base.update(path, record);
    }

    private Record delete(RecordStore base, String path)
    {
        journal.add(new JournalEntry("delete", path));
        return base.delete(path);
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    private static interface Executable
    {
        Record execute(RecordStore record);
    }

    public Record select()
    {
        return inMemoryStore.select();
    }

    /*

    Prepare:
     - dump the transactions updated data to disk.

    Commit:
     -> commit assumes that renames will not fail.
     a) backup existing data by renaming dir
     b) commit new data by renaming dir
     c) cleanup

    Rollback:
     a) rename .backup to data
     b) cleanup any leftover directories.

     */

    public boolean prepare()
    {
        // check journal size. If size > x, then flush
        if (journal.size() > 100)
        {
            journal.clear();

            // compact.
            // write contents to .active directory.
            File active = new File(persistenceDir, ACTIVE_TXN_DIR);
            if (!active.mkdirs())
            {
                return false;
            }

            DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(active);
            serialiser.serialise("", inMemoryStore.select(), true);

            return true;
        }
        else
        {
            return journal.prepare();
        }
    }

    public void commit()
    {
        // rename directories.
        // data -> .backup
        File data = new File(persistenceDir, PERSISTENT_DIR);
        File dotbackup = new File(persistenceDir, BACKUP_DIR);
        if (data.isDirectory())
        {
            FileSystemUtils.rename(data, dotbackup);
        }

        // .active -> .current
        File active = new File(persistenceDir, ACTIVE_TXN_DIR);
        if (active.isDirectory())
        {
            FileSystemUtils.rename(active, data);
        }

        if (dotbackup.isDirectory())
        {
            FileSystemUtils.rmdir(dotbackup);
        }

        journal.commit();
    }

    public void rollback()
    {
        recover();
        journal.rollback();
    }

    private void recover()
    {
        // process a recovery if necessary.
        File data = new File(persistenceDir, PERSISTENT_DIR);
        File backup = new File(persistenceDir, BACKUP_DIR);

        File active = new File(persistenceDir, ACTIVE_TXN_DIR);
        if (active.isDirectory())
        {
            FileSystemUtils.rmdir(active);
        }

        if (data.isDirectory())
        {
            FileSystemUtils.rmdir(backup);
        }
        else if (backup.isDirectory())
        {
            FileSystemUtils.rename(backup, data);
        }
    }
}
