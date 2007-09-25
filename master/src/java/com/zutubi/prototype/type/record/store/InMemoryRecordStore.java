package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.TransactionResource;
import com.zutubi.prototype.type.record.ImmutableRecord;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public class InMemoryRecordStore implements RecordStore, TransactionResource
{
    private MutableRecord base;

    private ThreadLocal<MutableRecord> baseHolder = new ThreadLocal<MutableRecord>();

    private TransactionManager transactionManager;

    public InMemoryRecordStore()
    {
        base = new MutableRecordImpl();
    }

    public InMemoryRecordStore(MutableRecord base)
    {
        this.base = base;
    }

    public Record insert(final String path, final Record record)
    {
        return execute(new Executable()
        {
            public Record execute(MutableRecord base)
            {
                return insert(base, path, record);
            }
        });
    }

    public Record update(final String path, final Record record)
    {
        return execute(new Executable()
        {
            public Record execute(MutableRecord base)
            {
                return update(base, path, record);
            }
        });
    }

    public Record delete(final String path)
    {
        return execute(new Executable()
        {
            public Record execute(MutableRecord base)
            {
                return delete(base, path);
            }
        });
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    private static interface Executable
    {
        Record execute(MutableRecord record);
    }
    
    private synchronized Record execute(Executable action)
    {
        // ensure that we are part of the transaction.
        boolean activeTransaction = transactionManager.getTransaction() != null;
        if (activeTransaction)
        {
            transactionManager.getTransaction().enlistResource(this);
        }

        MutableRecord writeableState = baseHolder.get();
        if (writeableState == null)
        {
            writeableState = base.copy(true);
            baseHolder.set(writeableState);
        }

        Record result = action.execute(writeableState);
        if (!activeTransaction)
        {
            commit();
        }
        return result;
    }
    
    private Record insert(MutableRecord base, String path, Record newRecord)
    {
        MutableRecord record = newRecord.copy(true);

        String[] parentElements = PathUtils.getParentPathElements(path);
        String basePath = PathUtils.getBaseName(path);

        MutableRecord parent = getRecord(base, parentElements);
        if (parent == null)
        {
            throw new IllegalArgumentException("No parent record for path '" + path + "'");
        }

        // Save first before hooking up in memory
        parent.put(basePath, record);

        return record;
    }

    public Record update(MutableRecord base, String path, Record updatedRecord)
    {
        String[] parentElements = PathUtils.getParentPathElements(path);
        String basePath = PathUtils.getBaseName(path);

        // quick validation.
        MutableRecord parentRecord = getRecord(base, parentElements);
        if (parentRecord == null)
        {
            throw new IllegalArgumentException("No parent record for path '" + path + "'");
        }

        MutableRecord targetRecord = getChildRecord(parentRecord, basePath);
        if (targetRecord == null)
        {
            throw new IllegalArgumentException("No record at path '" + path + "'");
        }

        // Create a new record to store the updates, and use it to replace the cached record.
        // The cached record is cut loose and will be collected when no longer in use.
        MutableRecord copy = targetRecord.copy(false);
        for (String key : updatedRecord.simpleKeySet())
        {
            copy.put(key, updatedRecord.get(key));
        }

        // Remove simple values not present in the input
        for (String key : targetRecord.simpleKeySet())
        {
            if (updatedRecord.get(key) == null)
            {
                copy.remove(key);
            }
        }

        // do the same for the meta data.
        for (String key : updatedRecord.metaKeySet())
        {
            copy.putMeta(key, updatedRecord.getMeta(key));
        }
        for (String key : targetRecord.metaKeySet())
        {
            if (updatedRecord.getMeta(key) == null)
            {
                copy.removeMeta(key);
            }
        }

        parentRecord.put(basePath, copy);
        return copy;
    }

    public Record delete(MutableRecord base, String path)
    {
        String[] parentPath = PathUtils.getParentPathElements(path);
        String basePath = PathUtils.getBaseName(path);
        
        MutableRecord parentRecord = getRecord(base, parentPath);
        if (parentRecord == null)
        {
            throw new IllegalArgumentException("No parent record for path '" + path + "'");
        }

        Object value = parentRecord.get(basePath);
        if (value != null && value instanceof Record)
        {
            return (Record) parentRecord.remove(basePath);
        }
        return null;
    }

    private MutableRecord getRecord(MutableRecord record, String[] pathElements)
    {
        for (String element : pathElements)
        {
            record = getChildRecord(record, element);
        }
        return record;
    }

    private MutableRecord getChildRecord(MutableRecord record, String element)
    {
        Object obj = record.get(element);
        if (obj == null || !(obj instanceof MutableRecordImpl))
        {
            return null;
        }
        return (MutableRecordImpl) obj;
    }

    public Record select()
    {
        MutableRecord record = baseHolder.get();
        if (record != null)
        {
            return new ImmutableRecord(record);
        }

        // ensure that no one external changes our internal state. 
        return new ImmutableRecord(base);
    }

    //---( transactional implementation )---
    
    public boolean prepare()
    {
        // no work required here.
        return true;
    }

    public void commit()
    {
        if (baseHolder.get() == null)
        {
            // no changes recorded.
            return;
        }

        base = baseHolder.get();
        baseHolder.set(null);
    }

    public void rollback()
    {
        // clear out any state data that we had.
        baseHolder.set(null);
    }

}
