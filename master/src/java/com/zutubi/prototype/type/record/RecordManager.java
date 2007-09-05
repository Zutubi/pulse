package com.zutubi.prototype.type.record;

import java.util.HashMap;
import java.util.LinkedList;
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

    private RecordManagerState readState;

    private RecordManagerState writeState;

    /**
     * The current highest handle allocated.
     */
    private AtomicLong nextHandle = new AtomicLong(UNDEFINED);
    
    private RecordSerialiser recordSerialiser;

    public void init()
    {
        // load the serialized records, recording handle - path mappings as well as
        // the highest issued handle.
        
        final long[] highest = {0L};
        readState = new RecordManagerState();
        readState.base = recordSerialiser.deserialise("", new RecordHandler()
        {
            public void handle(String path, Record record)
            {
                long handle = record.getHandle();
                if (handle > highest[0])
                {
                    highest[0] = handle;
                }

                readState.handleToPathMap.put(handle, path);
            }
        });

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
    public String getPathForHandle(long handle)
    {
        return readState.getPathForHandle(handle);
    }

    /**
     * Returns a list of all existing paths that match the given pattern.
     *
     * @param pattern pattern to match, may include wildcards
     * @return the paths of all records whose path matches the pattern
     */
    public List<String> getAllPaths(String pattern)
    {
        return readState.getAllPaths(pattern);
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

        return readState.select(path);
    }

    /**
     * Loads all records whose paths match the given path.  The path may
     * include wildcards.
     *
     * @param path    search path to use, may include wildcards
     * @return a map filled with a mapping from path to record for all records that are stored
     *  at a path matching the search path
     */
    public synchronized Map<String, Record> selectAll(String path)
    {
        checkPath(path);

        return readState.selectAll(PathUtils.getPathElements(path), 0, "");
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

        return readState.getRecord(PathUtils.getPathElements(path)) != null;
    }

    private synchronized Object execute(Action action)
    {
        writeState = new RecordManagerState(readState);
        try
        {
            // start transaction
            Object result = action.execute(writeState);
            commit();
            return result;
        }
        catch (RuntimeException e)
        {
            rollback();
            throw e;
        }
    }

    public void commit()
    {
        if (writeState == null)
        {
            // nothing to commit.
            return;
        }

        // Flush in memory changes to disk.
        recordSerialiser.serialise("", writeState.base, true);

        readState = writeState;
        writeState = null;
    }

    public void rollback()
    {
        writeState = null;
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

        return (Record) execute(new UpdateAction(path, values));
    }

    /**
     * Inserts a new record at the given path.  No record should exist at
     * that path, but the parent must exist.
     *
     * @param path      path to store the new record at
     * @param newRecord the record value to store
     * @return the newly-inserted record
     */
    public synchronized Record insert(String path, Record newRecord)
    {
        checkPath(path);

        return (Record) execute(new InsertAction(path, newRecord));
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
    public synchronized Record delete(String path)
    {
        checkPath(path);
        
        return (Record) execute(new DeleteAction(path));
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
            record = (Record) execute(new InsertAction(destinationPath, record, false));
        }
        return record;
    }

    public void setRecordSerialiser(RecordSerialiser recordSerialiser)
    {
        this.recordSerialiser = recordSerialiser;
    }

    private static interface Action
    {
        Object execute(RecordManagerState state);
    }

    private class InsertAction implements Action
    {
        private String path;
        private Record newRecord;
        private boolean allocateHandles;

        public InsertAction(String path, Record newRecord)
        {
            this(path, newRecord, true);
        }

        public InsertAction(String path, Record newRecord, boolean allocateHandles)
        {
            this.path = path;
            this.newRecord = newRecord;
            this.allocateHandles = allocateHandles;
        }

        public Record execute(RecordManagerState state)
        {
            MutableRecord record = newRecord.copy(true);
            if (allocateHandles)
            {
                allocateHandles(record);
            }

            return state.store(path, record);
        }
    }

    private class UpdateAction implements Action
    {
        private String path;
        private Record values;

        public UpdateAction(String path, Record values)
        {
            this.path = path;
            this.values = values;
        }

        public Record execute(RecordManagerState state)
        {

            return state.update(path, values);
        }
    }

    private class DeleteAction implements Action
    {
        private String path;

        public DeleteAction(String path)
        {
            this.path = path;
        }

        public Record execute(RecordManagerState state)
        {
            return state.remove(path);
        }
    }

    /**
     *
     */
    private class RecordManagerState
    {
        /**
         * The base record is the 'anchor' point for all of the records held in memory. All searches for
         * records start from here.
         */
        private MutableRecord base;

        /**
         * Map of record handles to paths
         */
        private Map<Long, String> handleToPathMap;

        public RecordManagerState()
        {
            handleToPathMap = new HashMap<Long, String>();
        }

        public RecordManagerState(RecordManagerState otherState)
        {
            this.base = otherState.base.copy(true);
            this.handleToPathMap  = new HashMap<Long, String>(otherState.handleToPathMap);
        }

        protected Record update(String path, Record values)
        {
            String[] parentElements = PathUtils.getParentPathElements(path);
            String baseName = PathUtils.getBaseName(path);

            MutableRecord parentRecord = getRecord(base, parentElements);
            if (parentRecord == null)
            {
                throw new IllegalArgumentException("No parent record for path '" + path + "'");
            }

            MutableRecord record = getChildRecord(parentRecord, baseName);
            if (record == null)
            {
                throw new IllegalArgumentException("No record at path '" + path + "'");
            }

            // Create a new record to store the updates, and use it to replace
            // the cached record.  The cached record is cut loose and will be
            // collected when no longer in use.
            MutableRecord copy = record.copy(false);
            for (String key : values.simpleKeySet())
            {
                copy.put(key, values.get(key));
            }

            // Remove simple values not present in the input
            for (String key : record.simpleKeySet())
            {
                if (values.get(key) == null)
                {
                    copy.remove(key);
                }
            }
            parentRecord.put(baseName, copy);
            return copy;
        }

        protected Record remove(String path)
        {
            MutableRecord parentRecord = getRecord(PathUtils.getParentPathElements(path));
            if (parentRecord == null)
            {
                throw new IllegalArgumentException("No parent record for path '" + path + "'");
            }

            String baseName = PathUtils.getBaseName(path);
            Object value = parentRecord.get(baseName);
            if (value != null && value instanceof Record)
            {
                Record result = (Record) parentRecord.remove(baseName);
                removeFromHandleMap(result);
                return result;
            }
            return null;
        }

        protected Record store(String path, MutableRecord record)
        {
            String[] pathElements = PathUtils.getPathElements(path);
            if (pathElements == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "'");
            }

            MutableRecord parent = getRecord(PathUtils.getParentPathElements(pathElements));
            if (parent == null)
            {
                throw new IllegalArgumentException("No parent record for path '" + path + "'");
            }

            // Save first before hooking up in memory
            parent.put(pathElements[pathElements.length - 1], record);

            // We can't do this at handle allocation time as it needs to work
            // when moving a record.
            addToHandleMap(path, record);
            return record;
        }

        protected Record select(String path)
        {
            String[] elements = PathUtils.getPathElements(path);

            Record record = this.base;
            for (String pathElement : elements)
            {
                Object data = record.get(pathElement);
                if (data == null || !(data instanceof Record))
                {
                    return null;
                }
                record = (Record) data;
            }
            return record;
        }

        protected Map<String, Record> selectAll(String[] elements, int pathIndex, String resolvedPath)
        {
            Map<String, Record> records = new HashMap<String, Record>();
            selectAll(this.base, elements, pathIndex, resolvedPath, records);
            return records;
        }

        protected void selectAll(Record record, String[] elements, int pathIndex, String resolvedPath, Map<String, Record> records)
        {
            if (pathIndex == elements.length)
            {
                records.put(resolvedPath, record);
                return;
            }

            for (String key : record.nestedKeySet())
            {
                if (PathUtils.elementMatches(elements[pathIndex], key))
                {
                    selectAll((Record) record.get(key), elements, pathIndex + 1, PathUtils.getPath(resolvedPath, key), records);
                }
            }
        }

        public List<String> getAllPaths(String pattern)
        {
            List<String> allPaths = new LinkedList<String>();
            getAllPaths(this.base, PathUtils.getPathElements(pattern), 0, "", allPaths);
            return allPaths;
        }

        private void getAllPaths(Record record, String[] elements, int pathIndex, String resolvedPath, List<String> paths)
        {
            if (pathIndex == elements.length)
            {
                paths.add(resolvedPath);
            }
            else
            {
                for (String key : record.nestedKeySet())
                {
                    if (PathUtils.elementMatches(elements[pathIndex], key))
                    {
                        getAllPaths((Record) record.get(key), elements, pathIndex + 1, PathUtils.getPath(resolvedPath, key), paths);
                    }
                }
            }
        }

        public String getPathForHandle(long handle)
        {
            return handleToPathMap.get(handle);
        }

        private void addToHandleMap(String path, MutableRecord record)
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

        protected MutableRecord getRecord(String[] pathElements)
        {
            return getRecord(this.base, pathElements);
        }

        protected MutableRecord getRecord(MutableRecord record, String[] pathElements)
        {
            for (String element : pathElements)
            {
                record = getChildRecord(record, element);
            }
            return record;
        }

        protected MutableRecord getChildRecord(MutableRecord record, String element)
        {
            Object obj = record.get(element);
            if (obj == null || !(obj instanceof MutableRecordImpl))
            {
                return null;
            }
            return (MutableRecordImpl) obj;
        }
    }
}
