package com.zutubi.tove.type.record;

import com.zutubi.events.EventManager;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.inmemory.InMemoryMapStateWrapper;
import com.zutubi.tove.transaction.inmemory.InMemoryTransactionResource;
import com.zutubi.tove.type.record.events.RecordDeletedEvent;
import com.zutubi.tove.type.record.events.RecordInsertedEvent;
import com.zutubi.tove.type.record.events.RecordUpdatedEvent;
import com.zutubi.tove.type.record.store.RecordStore;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.logging.Logger;

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
 * {@link com.zutubi.tove.config.ConfigurationTemplateManager} should
 * be used.
 */
public class RecordManager implements HandleAllocator
{
    private static final Logger LOG = Logger.getLogger(RecordManager.class);

    public static final long UNDEFINED = 0;

    /**
     * Record handle to path map.  Handles uniquely identify paths, so there should
     * never be any duplication.  This object aggressively enforces this.
     */
    private InMemoryTransactionResource<Map<Long, String>> state;

    /**
     * The current highest handle allocated.
     */
    private AtomicLong nextHandle = new AtomicLong(UNDEFINED);

    private RecordStore recordStore;

    private TransactionManager transactionManager;
    private EventManager eventManager;

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
                // sanity check for duplicate handles.
                if (handleToPathMapping.containsKey(handle))
                {
                    LOG.severe("Duplicate record handle detected for handle " + handle + " and paths '" +
                            path + "' and '" + handleToPathMapping.get(handle) + "'");
                }
                handleToPathMapping.put(handle, path);
            }
        });

        state = new InMemoryTransactionResource<Map<Long, String>>(new InMemoryMapStateWrapper<Long, String>(handleToPathMapping));
        state.setTransactionManager(transactionManager);

        nextHandle.set(highest[0]);
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
    public synchronized String getPathForHandle(long handle)
    {
        return state.get(false).get(handle);
    }

    /**
     * Returns a list of all existing paths that match the given pattern.
     *
     * @param pattern pattern to match, may include wildcards
     * @return the paths of all records whose path matches the pattern
     */
    public synchronized List<String> getAllPaths(String pattern)
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
        checkPath(path, false);

        RecordQueries queries = new RecordQueries(recordStore.select());
        return queries.select(path);
    }

    public synchronized Record select()
    {
        return recordStore.select();
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
        checkPath(pattern, false);

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
        checkPath(path, false);

        return select(path) != null;
    }

    /**
     * Updates the record at the given path with the new values.  Only simple
     * values are updated: child records are unaffected.  Values will also be
     * removed if they do not exist in the given record.
     *
     * @param path   path of the record to update: a record must exist at
     *               this path
     * @param values a record holding new simple values to apply
     */
    public synchronized void update(final String path, final Record values)
    {
        checkPath(path, false);

        // sanity check - we expect the states handle/path to match that of the
        // record we are updating.  If not we fail early.
        Record originalRecord = select(path);
        if (originalRecord != null) // our aim is to check the handle, not if a record exists.
        {
            if (values.getHandle() != originalRecord.getHandle())
            {
                throw new IllegalArgumentException("Failed to update '" + path + "'. New handle differs from existing handle.");
            }
        }
        recordStore.update(path, values);

        eventManager.publish(new RecordUpdatedEvent(this, path, originalRecord, new ImmutableRecord(values)));
    }

    /**
     * Inserts a new record at the given path.  No record should exist at
     * that path, but the parent must exist.
     *
     * @param path      path to store the new record at
     * @param record    the record value to store
     */
    public synchronized void insert(final String path, final Record record)
    {
        checkPath(path, true);

        if (record == null)
        {
            throw new IllegalArgumentException("Attempt to insert null record.");
        }

        // we copy first because we do not want to modify the argument.
        final MutableRecord copy = record.copy(true, true);
        allocateHandles(copy);

        transactionManager.runInTransaction(new NullaryFunction<Object>()
        {
            public Object process()
            {
                Map<Long, String> handleToPathMap = state.get(true);
                addToHandleMap(handleToPathMap, path, copy);
                recordStore.insert(path, copy);
                return null;
            }
        });

        eventManager.publish(new RecordInsertedEvent(this, path));
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
        checkPath(path, true);

        Record record = transactionManager.runInTransaction(new NullaryFunction<Record>()
        {
            public Record process()
            {
                Record deletedRecord = recordStore.delete(path);
                if (deletedRecord != null)
                {
                    Map<Long, String> handleToPathMap = state.get(true);
                    removeFromHandleMap(handleToPathMap, deletedRecord);
                }
                return deletedRecord;
            }
        });

        if (record != null)
        {
            eventManager.publish(new RecordDeletedEvent(this, path));
        }
        return record;
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
    public synchronized Record move(final String sourcePath, final String destinationPath)
    {
        checkPath(destinationPath, true);
        checkDoesNotExist(destinationPath, "Failed to move to destination path: '" + destinationPath + "'. An entry already exists at this path.");

        Record record = transactionManager.runInTransaction(new NullaryFunction<Record>()
        {
            public Record process()
            {
                Record record = delete(sourcePath);
                if (record != null)
                {
                    Map<Long, String> handleToPathMap = state.get(true);
                    addToHandleMap(handleToPathMap, destinationPath, record);
                    recordStore.insert(destinationPath, record);
                }
                return record;
            }
        });

        if (record != null)
        {
            eventManager.publish(new RecordInsertedEvent(this, destinationPath));
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

    private void allocateHandles(MutableRecord record)
    {
        record.setHandle(allocateHandle());
        for (Object child : record.values())
        {
            if (child instanceof MutableRecord)
            {
                allocateHandles((MutableRecord) child);
            }
        }
    }

    private void checkPath(String path, boolean disallowEmpty)
    {
        if (path == null || disallowEmpty && path.length() == 0)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
    }

    private void checkDoesNotExist(String destinationPath, String message)
    {
        if (containsRecord(destinationPath))
        {
            throw new IllegalArgumentException(message);
        }
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

    private void addToHandleMap(Map<Long, String> handleToPathMap, String path, Record record)
    {
        // aggressively ensure that we reject duplicates handles.
        if (handleToPathMap.containsKey(record.getHandle()))
        {
            // only time we will allow this is when the path is the same.
            String mappedPath = handleToPathMap.get(record.getHandle());
            if (!path.equals(mappedPath))
            {
                throw new RuntimeException("Attempting to add a duplicate record handle.  " +
                        "Existing path is: '" + mappedPath + "', duplicate path is: '" + path + "'");
            }
        }

        handleToPathMap.put(record.getHandle(), path);
        for (String key : record.nestedKeySet())
        {
            addToHandleMap(handleToPathMap, PathUtils.getPath(path, key), (MutableRecord) record.get(key));
        }
    }

    protected void removeFromHandleMap(Map<Long, String> handleToPathMap, Record record)
    {
        handleToPathMap.remove(record.getHandle());
        for (String key : record.nestedKeySet())
        {
            removeFromHandleMap(handleToPathMap, (Record) record.get(key));
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    private interface RecordHandler
    {
        void handle(String path, Record record);
    }
}
