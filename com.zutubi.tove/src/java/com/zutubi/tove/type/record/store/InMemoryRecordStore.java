package com.zutubi.tove.type.record.store;

import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.inmemory.InMemoryStateWrapper;
import com.zutubi.tove.transaction.inmemory.InMemoryTransactionResource;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.NullaryFunction;

import java.util.HashSet;
import java.util.Set;

public class InMemoryRecordStore implements RecordStore
{
    private TransactionManager transactionManager;
    private InMemoryTransactionResource<RecordHolder> state;

    private RecordHolder holder;

    public InMemoryRecordStore()
    {
        this(new MutableRecordImpl());
    }

    public InMemoryRecordStore(MutableRecord base)
    {
        if (base == null)
        {
            throw new IllegalArgumentException();
        }

        holder = new RecordHolder(base);
    }

    public void init()
    {
        state = new InMemoryTransactionResource<RecordHolder>(new TransactionalRecordHolder(holder));
        state.setTransactionManager(transactionManager);
    }

    public void insert(final String path, final Record record)
    {
        transactionManager.runInTransaction(new Runnable()
        {
            public void run()
            {
                insert(isolate(path), path, record);
            }
        });
    }

    public void update(final String path, final Record record)
    {
        transactionManager.runInTransaction(new Runnable()
        {
            public void run()
            {
                update(isolate(path), path, record);
            }
        });
    }

    public Record delete(final String path)
    {
        return transactionManager.runInTransaction(new NullaryFunction<Record>()
        {
            public Record process()
            {
                return delete(isolate(path), path);
            }
        });
    }

    public Record select()
    {
        // Ensure the internal integrity by returning an immutable reference to the record.
        return new ImmutableRecord(state.get(false).record);
    }

    public Record exportRecords()
    {
        return select();
    }

    public void importRecords(final Record r)
    {
        transactionManager.runInTransaction(new Runnable()
        {
            public void run()
            {
                importRecords(state.get(true).record, r);
            }
        });
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
        // quick validation.
        MutableRecord targetRecord = getRecord(base, PathUtils.getPathElements(path));
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

    private void importRecords(MutableRecord base, Record r)
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

    /**
     * Isolate the specified path, meaning that anyone that previously acquired a reference
     * to the record will no longer see any changes made to the record at the specified path
     * or its nested records.
     *
     * Essentially, any existing selected records that have access to the specified path will
     * not see any updates made to that path.
     *
     * @param path the path to be isolated
     * @return the new root record
     */
    private MutableRecord isolate(String path)
    {
        String[] pathElements = PathUtils.getPathElements(path);

        RecordHolder holder = state.get(true);
        holder.record = holder.record.copy(false, true);
        MutableRecord parent = holder.record;
        for (String pathElement : pathElements)
        {
            Record r = (Record) parent.get(pathElement);
            if (r == null)
            {
                return holder.record;
            }

            MutableRecord copy = r.copy(false, true);
            parent.put(pathElement, copy);
            parent = copy;
        }


        return holder.record;
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    /**
     * This simply adds a layer of indirection to the root record, so we don't have to copy the
     * root record every time we hand it out.  The outside world never sees this holder so we don't
     * need to copy it when isolating records.
     */
    private static class RecordHolder
    {
        public MutableRecord record;

        private RecordHolder(MutableRecord record)
        {
            this.record = record;
        }
    }

    private static class TransactionalRecordHolder extends InMemoryStateWrapper<RecordHolder>
    {
        private TransactionalRecordHolder(RecordHolder holder)
        {
            super(holder);
        }

        @Override
        protected InMemoryStateWrapper<RecordHolder> copy()
        {
            return new TransactionalRecordHolder(new RecordHolder(get().record.copy(true, true)));
        }
    }
}
