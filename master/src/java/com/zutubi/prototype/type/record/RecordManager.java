package com.zutubi.prototype.type.record;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.TransactionalWrapper;
import com.zutubi.prototype.type.record.store.RecordStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages a tree of records used to store arbitrary data.  All records are
 * cached in memory and backed by permanent storage.  This manager supports
 * basic CRUD operations, and manages the cache: including ensuring
 * consistency for loaded records.
 * <p/>
 * This class should not usually be accessed directly.  Rather, the
 * {@link com.zutubi.prototype.config.ConfigurationTemplateManager} should
 * be used.
 */
public class RecordManager implements HandleAllocator
{
    private static final long UNDEFINED = 0;

    private TransactionalWrapper<RecordManagerState> stateWrapper;

    /**
     * The current highest handle allocated.
     */
    private AtomicLong nextHandle = new AtomicLong(UNDEFINED);
    
    private RecordStore recordStore;

    private TransactionManager transactionManager;

    public void init()
    {
        final long[] highest = {0L};

        final Map<Long, String> handleToPathMapping = new HashMap<Long, String>();

        Record baseRecord = recordStore.select();
        traverse(baseRecord, new RecordHandler()
        {
            public void handle(String path, Record record)
            {
                long handle = record.getHandle();
                if (handle > highest[0])
                {
                    highest[0] = handle;
                }
                handleToPathMapping.put(handle, path);
            }
        });

        stateWrapper = new RecordManagerStateTransactionalWrapper(new RecordManagerState(handleToPathMapping));
        stateWrapper.setTransactionManager(transactionManager);
        
        nextHandle.set(highest[0]);
    }

    private void traverse(Record record, RecordHandler handler)
    {
        traverse("", record, handler);
    }

    private void traverse(String path, Record record, RecordHandler handler)
    {
        for (String key : record.keySet())
        {
            Object value = record.get(key);
            if (value instanceof Record)
            {
                traverse(PathUtils.getPath(path, key), (Record) value, handler);
            }
        }

        handler.handle(path, record);
    }

    public long allocateHandle()
    {
        return nextHandle.incrementAndGet();
    }

    /**
     * @param handle handle to look up
     * @return the path for the record with the given handle, or null if no
     *         record has that handle
     */
    public String getPathForHandle(long handle)
    {
        return getState().getPathForHandle(handle);
    }

    /**
     * Returns a list of all existing paths that match the given pattern.
     *
     * @param pattern pattern to match, may include wildcards
     * @return the paths of all records whose path matches the pattern
     */
    public List<String> getAllPaths(String pattern)
    {
        RecordQueries queries = new RecordQueries(recordStore.select());
        return queries.selectPaths(pattern);
    }

    /**
     * Load the record identified by the path.
     *
     * @param path uniquely identifying the record to be loaded.
     * @return the loaded record, or null if no record could be found.
     */
    public synchronized Record select(String path)
    {
        checkPath(path);

        RecordQueries queries = new RecordQueries(recordStore.select());
        return queries.select(path);
    }

    /**
     * Loads all records whose paths match the given path.  The path may
     * include wildcards.
     *
     * @param pattern    search path to use, may include wildcards
     * @return a map filled with a mapping from path to record for all records that are stored
     *  at a path matching the search path
     */
    public synchronized Map<String, Record> selectAll(String pattern)
    {
        checkPath(pattern);

        RecordQueries queries = new RecordQueries(recordStore.select());
        return queries.selectAll(pattern);
    }

    /**
     * Returns true if a record exists at the specified path.
     *
     * @param path uniquely identifying a record.
     * @return true if a record exists, false otherwise.
     */
    public synchronized boolean containsRecord(String path)
    {
        checkPath(path);

        return select(path) != null;
    }

    private RecordManagerState getState()
    {
        return stateWrapper.get();
    }

    private void allocateHandles(MutableRecord record)
    {
        if (record.getHandle() == UNDEFINED)
        {
            record.setHandle(allocateHandle());
        }
        for (Object child : record.values())
        {
            if (child instanceof MutableRecord)
            {
                allocateHandles((MutableRecord) child);
            }
        }
    }

    private void checkPath(String path)
    {
        if (path == null || path.equals(""))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
    }

    /**
     * Updates the record at the given path with the new values.  Only simple
     * values are updated: child records are unaffected.  Values will also be
     * removed if they do not exist in the given record.
     *
     * @param path   path of the record to update: a record must exist at
     *               this path
     * @param values a record holding new simple values to apply
     * @return the new record created by the update
     */
    public synchronized Record update(String path, Record values)
    {
        checkPath(path);
        
        return recordStore.update(path, values);
    }

    /**
     * Inserts a new record at the given path.  No record should exist at
     * that path, but the parent must exist.
     *
     * @param path      path to store the new record at
     * @param newRecord the record value to store
     * @return the newly-inserted record
     */
    public synchronized Record insert(final String path, final Record newRecord)
    {
        checkPath(path);

        return (Record) stateWrapper.execute(new TransactionalWrapper.Action<RecordManagerState>()
        {
            public Object execute(RecordManagerState state)
            {
                allocateHandles((MutableRecord) newRecord);
                state.addToHandleMap(path, newRecord);
                return recordStore.insert(path, newRecord);
            }
        });

    }

    /**
     * Stores the given record at the given path.  If a record exists at the
     * path, it is updated.  If not, a new record is inserted.  In the latter
     * case the parent record must exist.
     *
     * @param path   path to store the record at
     * @param record record values to store
     * @return the newly-stored record
     */
    public synchronized Record insertOrUpdate(String path, Record record)
    {
        if (containsRecord(path))
        {
            return update(path, record);
        }
        else
        {
            return insert(path, record);
        }
    }

    /**
     * Delete the record at the specified path.
     *
     * @param path identifying the record to be deleted
     * @return an instance of the record just deleted, or null if no record
     *         exists at the specified path.
     */
    public synchronized Record delete(final String path)
    {
        checkPath(path);

        return (Record) stateWrapper.execute(new TransactionalWrapper.Action<RecordManagerState>()
        {
            public Object execute(RecordManagerState state)
            {
                Record deletedRecord = recordStore.delete(path);
                if (deletedRecord != null)
                {
                    state.removeFromHandleMap(deletedRecord);
                }
                return deletedRecord;
            }
        });
    }

    /**
     * Copy the record contents from the source path to the destination path.
     * A new record with a new handle is created at the destination.
     *
     * @param sourcePath      path to copy from
     * @param destinationPath path to copy to
     * @return the new record, or null if the source path does not refer to
     *         an existing record
     */
    public synchronized Record copy(String sourcePath, String destinationPath)
    {
        MutableRecord record = (MutableRecord) select(sourcePath);
        if (record != null)
        {
            MutableRecord copy = record.copy(true);
            insert(destinationPath, copy);
            return copy;
        }

        return null;
    }

    /**
     * Move the record at the given source to the given destination.  The
     * record handle is maintained.
     *
     * @param sourcePath      path to move from
     * @param destinationPath path to move to
     * @return the moved record, or null if the source path does not refer to
     *         an existing record
     */
    public synchronized Record move(String sourcePath, String destinationPath)
    {
        Record record = delete(sourcePath);
        if (record != null)
        {
            checkPath(destinationPath);
            record = insert(destinationPath, record);
        }
        return record;
    }

    public void setRecordStore(RecordStore recordStore)
    {
        this.recordStore = recordStore;
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    /**
     *
     */
    private class RecordManagerState
    {
        Map<Long, String> handleToPathMap;

        public RecordManagerState()
        {
            this(new HashMap<Long, String>());
        }

        public RecordManagerState(Map<Long, String> handleToPathMap)
        {
            this.handleToPathMap = new HashMap<Long, String>(handleToPathMap);
        }

        public RecordManagerState(RecordManagerState otherState)
        {
            this.handleToPathMap  = new HashMap<Long, String>(otherState.handleToPathMap);
        }

        public String getPathForHandle(long handle)
        {
            return handleToPathMap.get(handle);
        }

        private void addToHandleMap(String path, Record record)
        {
            handleToPathMap.put(record.getHandle(), path);
            for (String key : record.nestedKeySet())
            {
                addToHandleMap(PathUtils.getPath(path, key), (MutableRecord) record.get(key));
            }
        }

        protected void removeFromHandleMap(Record record)
        {
            handleToPathMap.remove(record.getHandle());
            for (String key : record.nestedKeySet())
            {
                removeFromHandleMap((Record) record.get(key));
            }
        }
    }

    private class RecordManagerStateTransactionalWrapper extends TransactionalWrapper<RecordManagerState>
    {
        public RecordManagerStateTransactionalWrapper(RecordManagerState global)
        {
            super(global);
        }

        public RecordManagerState copy(RecordManagerState o)
        {
            return new RecordManagerState(o);
        }
    }
}
