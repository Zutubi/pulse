package com.zutubi.tove.type.record.store;

import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.TransactionalWrapper;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.UnaryFunction;

import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 */
public class InMemoryRecordStore implements RecordStore
{
    private TransactionalWrapper<MutableRecordHolder> wrapper;

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

        wrapper = new MutableRecordHolderTransactionalWrapper(new MutableRecordHolder(base));
        wrapper.setTransactionManager(new TransactionManager());
    }

    public void insert(final String path, final Record record)
    {
        // all methods that modify the internal structure of this memory store
        // are wrapped.
        wrapper.execute(new UnaryFunction<MutableRecordHolder, Object>()
        {
            public Object process(MutableRecordHolder base)
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
        wrapper.execute(new UnaryFunction<MutableRecordHolder, Object>()
        {
            public Object process(MutableRecordHolder base)
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
        return wrapper.execute(new UnaryFunction<MutableRecordHolder, Record>()
        {
            public Record process(MutableRecordHolder base)
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
        wrapper.execute(new UnaryFunction<MutableRecordHolder, Object>()
        {
            public Object process(MutableRecordHolder holder)
            {
                // update the base to contain the contents of r.
                MutableRecord base = new MutableRecordImpl();
                for (String key : r.keySet())
                {
                    base.put(key, r.get(key));
                }
                for (String key : r.metaKeySet())
                {
                    base.put(key, r.getMeta(key));
                }
                holder.setRecord(base);
                
                return null;
            }
        });
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        wrapper.setTransactionManager(transactionManager);
    }

    private Record insert(MutableRecordHolder holder, String path, Record newRecord)
    {
        MutableRecord base = holder.getRecord().copy(true, true);

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

        holder.setRecord(base);

        return record;
    }

    private Record update(MutableRecordHolder holder, String path, Record updatedRecord)
    {
        MutableRecord base = holder.getRecord().copy(true, true);

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

        holder.setRecord(base);
        
        return targetRecord;
    }

    private Record delete(MutableRecordHolder holder, String path)
    {
        MutableRecord base = holder.getRecord().copy(true, true);

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
            Record result = (Record) parentRecord.remove(baseName);
            holder.setRecord(base);
            return result;
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
        // Ensure the internal integrity by returning an immutable reference to the record.
        return new ImmutableRecord(wrapper.get().getRecord());
    }

    private class MutableRecordHolderTransactionalWrapper extends TransactionalWrapper<MutableRecordHolder>
    {
        public MutableRecordHolderTransactionalWrapper(MutableRecordHolder global)
        {
            super(global);
        }

        public MutableRecordHolder copy(MutableRecordHolder v)
        {
            return v.copy();
        }
    }

    private class MutableRecordHolder
    {
        private MutableRecord record;

        public MutableRecordHolder(MutableRecord record)
        {
            this.record = record;
        }

        public MutableRecord getRecord()
        {
            return record;
        }

        public void setRecord(MutableRecord record)
        {
            this.record = record;
        }

        public MutableRecordHolder copy()
        {
            return new MutableRecordHolder(record.copy(true, true));
        }
    }
}
