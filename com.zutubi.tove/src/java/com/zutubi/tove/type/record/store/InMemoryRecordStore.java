package com.zutubi.tove.type.record.store;

import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.TransactionalWrapper;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.UnaryFunction;

import java.util.HashSet;
import java.util.Set;

/**
 * An in memory implementation of the Record Store interface.
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

        wrapper = new MutableRecordTransactionalWrapper(base);
        wrapper.setTransactionManager(new TransactionManager());
    }

    public void insert(final String path, final Record record)
    {
        wrapper.execute(new UnaryFunction<MutableRecord, Object>()
        {
            public Object process(MutableRecord base)
            {
                isolate(PathUtils.getParentPath(path));
                insert(base, path, record);
                return null;
            }
        });
    }

    public void update(final String path, final Record record)
    {
        wrapper.execute(new UnaryFunction<MutableRecord, Object>()
        {
            public Object process(MutableRecord base)
            {
                isolate(path);
                update(base, path, record);
                return null;
            }
        });
    }

    public Record delete(final String path)
    {
        return wrapper.execute(new UnaryFunction<MutableRecord, Record>()
        {
            public Record process(MutableRecord base)
            {
                isolate(path);
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
        wrapper.execute(new UnaryFunction<MutableRecord, Object>()
        {
            public Object process(MutableRecord base)
            {
                // update the base to contain the contents of r.
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
        MutableRecord record = newRecord.copy(true, true);

        parent.put(baseName, record);

        return record;
    }

    private Record update(MutableRecord base, String path, Record updatedRecord)
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

        for (String key : updatedRecord.simpleKeySet())
        {
            targetRecord.put(key, updatedRecord.get(key));
        }

        // Remove simple values not present in the input
        Set<String> keys = new HashSet<String>(targetRecord.simpleKeySet());
        for (String key : keys)
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
        keys = new HashSet<String>(targetRecord.metaKeySet());
        for (String key : keys)
        {
            if (updatedRecord.getMeta(key) == null)
            {
                targetRecord.removeMeta(key);
            }
        }

        return targetRecord;
    }

    private Record delete(MutableRecord base, String path)
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
        MutableRecord root = wrapper.get();

        // Copy the root so that we do not need to copy it during the isolation processing.
        // It is much simpler if we never give out a reference to the root record.
        MutableRecord clone = new MutableRecordImpl();
        for (String key : root.metaKeySet())
        {
            clone.putMeta(key, root.getMeta(key));
        }

        for (String key : root.keySet())
        {
            clone.put(key, root.get(key));
        }

        // Ensure the internal integrity by returning an immutable reference to the record.
        return new ImmutableRecord(clone);
    }

    /**
     * Isolate the specified path, meaning that anyone that previously acquired a reference
     * to the record will no longer see any changes made to the record at the specified path
     * or its nested records.
     *
     * Essentially, any existing selected records that have access to the specified path will
     * not see any updates made to that path.
     *
     * @param path  the path to be isolated
     */
    public void isolate(String path)
    {
        MutableRecord root = wrapper.get();

        String[] pathElements = PathUtils.getPathElements(path);

        MutableRecord parent = root;
        for (String pathElement : pathElements)
        {
            Record r = (Record) parent.get(pathElement);
            if (r == null)
            {
                return;
            }
            
            MutableRecord copy = r.copy(false, true);
            parent.put(pathElement, copy);
            parent = copy;
        }
    }

    /**
     * Extension of the TransactionalWrapper that holds a mutable record as its
     * transactional data.
     */
    private class MutableRecordTransactionalWrapper extends TransactionalWrapper<MutableRecord>
    {
        public MutableRecordTransactionalWrapper(MutableRecord global)
        {
            super(global);
        }

        public MutableRecord copy(MutableRecord v)
        {
            System.out.println("InMemoryRecordStore$MutableRecordTransactionalWrapper.copy");
            return v.copy(true, true);
        }
    }
}
