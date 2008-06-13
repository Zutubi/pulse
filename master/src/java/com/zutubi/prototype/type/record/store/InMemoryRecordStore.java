package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.TransactionalWrapper;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public class InMemoryRecordStore implements RecordStore
{
    private TransactionalWrapper<MutableRecord> wrapper;

    public InMemoryRecordStore()
    {
        this(new MutableRecordImpl());
    }

    public InMemoryRecordStore(MutableRecord base)
    {
        if (base == null)
        {
            base = new MutableRecordImpl();
        }

        // use a default transaction manager.  This will not participate in any of the
        // external transactions and as such act as a noop transaction manager.  Why use it then?
        // To allow this record store to be used in the absence of an external transaction manager.

        wrapper = new MutableRecordTransactionalWrapper(base);
        wrapper.setTransactionManager(new TransactionManager());
    }

    public void insert(final String path, final Record record)
    {
        // all methods that modify the internal structure of this memory store
        // are wrapped.
        wrapper.execute(new TransactionalWrapper.Action<MutableRecord>()
        {
            public Object execute(MutableRecord base)
            {
                insert(base, path, record);
                return null;
            }
        });
    }

    public void update(final String path, final Record record)
    {
        // all methods that modify the internal structure of this memory store
        // are wrapped.
        wrapper.execute(new TransactionalWrapper.Action<MutableRecord>()
        {
            public Object execute(MutableRecord base)
            {
                update(base, path, record);
                return null;
            }
        });
    }

    public Record delete(final String path)
    {
        // all methods that modify the internal structure of this memory store
        // are wrapped.
        return (Record) wrapper.execute(new TransactionalWrapper.Action<MutableRecord>()
        {
            public Object execute(MutableRecord base)
            {
                return delete(base, path);
            }
        });
    }

    public Record exportRecords()
    {
        return select();
    }

    public void importRecords(final Record r)
    {
        // all methods that modify the internal structure of this memory store
        // are wrapped.
        wrapper.execute(new TransactionalWrapper.Action<MutableRecord>()
        {
            public Object execute(MutableRecord base)
            {
                // update the base to contain the contents of r.
                base.clear();
                for (String key : r.keySet())
                {
                    base.put(key, r.get(key));
                }
                for (String key : r.metaKeySet())
                {
                    base.put(key, r.getMeta(key));
                }
                return null;
            }
        });
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        wrapper.setTransactionManager(transactionManager);
    }

    private Record insert(MutableRecord base, String path, Record newRecord)
    {
        String[] parentPathElements = PathUtils.getParentPathElements(path);
        String baseName = PathUtils.getBaseName(path);

        MutableRecord parent = getRecord(base, parentPathElements);
        // we do not 'create the path' on insert.  Therefore, the parent must exist.
        if (parent == null)
        {
            throw new IllegalArgumentException("No parent record for path '" + path + "'");
        }

        Object obj = parent.get(baseName);
        if (obj != null)
        {
            throw new IllegalArgumentException("Can not insert a new record.  Value already exists at '"+path+"'.");
        }

        // The external client may hold on to a reference of the newRecord, and may change it.  To
        // ensure the integrity of the internal datastructure of the record sture, we must run a
        // deep copy prior to the insertion so that no external reference remains.
        MutableRecord record = newRecord.copy(true);
        
        parent.put(baseName, record);

        return record;
    }

    public Record update(MutableRecord base, String path, Record updatedRecord)
    {
        String[] parentPathElements = PathUtils.getParentPathElements(path);
        String baseName = PathUtils.getBaseName(path);

        // quick validation.
        MutableRecord parentRecord = getRecord(base, parentPathElements);
        if (parentRecord == null)
        {
            throw new IllegalArgumentException("No record for path '" + path + "'");
        }

        MutableRecord targetRecord = getChildRecord(parentRecord, baseName);
        if (targetRecord == null)
        {
            throw new IllegalArgumentException("No record for path '" + path + "'");
        }

/*
        // Create a new record to store the updates, and use it to replace the cached record.
        // The cached record is cut loose and will be collected when no longer in use.
        MutableRecord copy = targetRecord.copy(false);
        // the targetRecord is an internal instance that will never be referenced externally
        // Therefore, it does not need to be 'copied'.
*/
        for (String key : updatedRecord.simpleKeySet())
        {
            targetRecord.put(key, updatedRecord.get(key));
        }

        // Remove simple values not present in the input
        for (String key : targetRecord.simpleKeySet())
        {
            if (updatedRecord.get(key) == null)
            {
                targetRecord.remove(key);
            }
        }

        // do the same for the meta data.
        for (String key : updatedRecord.metaKeySet())
        {
            targetRecord.putMeta(key, updatedRecord.getMeta(key));
        }
        for (String key : targetRecord.metaKeySet())
        {
            if (updatedRecord.getMeta(key) == null)
            {
                targetRecord.removeMeta(key);
            }
        }

//        parentRecord.put(baseName, copy);
        return targetRecord;
    }

    public Record delete(MutableRecord base, String path)
    {
        String[] parentPathElements = PathUtils.getParentPathElements(path);
        String baseName = PathUtils.getBaseName(path);

        MutableRecord parentRecord = getRecord(base, parentPathElements);
        if (parentRecord == null)
        {
            // no record can exist at the specified path, so return null to
            // indicate this.
            return null;
        }

        MutableRecord targetRecord = getChildRecord(parentRecord, baseName);
        if (targetRecord != null)
        {
            return (Record) parentRecord.remove(baseName);
        }

        // paths need to refer to records, not values within a record. Therefore,
        // since value is not a record, the path does not refer to a record to be
        // removed.
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
        if (record == null)
        {
            return null;
        }
        
        Object obj = record.get(element);
        if (obj == null || !(obj instanceof MutableRecord))
        {
            return null;
        }
        return (MutableRecord) obj;
    }

    public Record select()
    {
        // copy on select to ensure that the internal data structure of this record store
        // retains its integrity by ensuring that it can not be accessed without going through
        // the public interface of this class.
        // This will slow things down to some degree.
        // This copy on select also protects the client from changes to the structure they
        // are holding when the internal data structure is modified.
        return wrapper.get().copy(true);
    }

    private class MutableRecordTransactionalWrapper extends TransactionalWrapper<MutableRecord>
    {
        public MutableRecordTransactionalWrapper(MutableRecord global)
        {
            super(global);
        }

        public MutableRecord copy(MutableRecord v)
        {
            return v.copy(true);
        }
    }
}
