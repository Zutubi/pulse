package com.zutubi.prototype.type.record;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages a tree of records used to store arbitrary data.  All records are
 * cached in memory and backed by permanent storage.  This manager supports
 * basic CRUD operations, and manages the cache: including ensuring
 * consistency for loaded records.
 *
 * This class should not usually be accessed directly.  Rather, the
 * {@link com.zutubi.prototype.config.ConfigurationTemplateManager} should
 * be used.
 */
public class RecordManager implements HandleAllocator
{
    private static final long UNDEFINED = 0;

    /**
     * The base record is the 'anchor' point for all of the records held in memory. All searches for
     * records start from here.
     */
    private MutableRecord baseRecord;
    private Map<Long, String> handleToPathMap = new HashMap<Long, String>();
    private AtomicLong nextHandle = new AtomicLong(UNDEFINED);
    private RecordSerialiser recordSerialiser;

    public void init()
    {
        final long[] highest = { 0L };
        baseRecord = recordSerialiser.deserialise("", new RecordHandler()
        {
            public void handle(String path, Record record)
            {
                long handle = record.getHandle();
                if(handle > highest[0])
                {
                    highest[0] = handle;
                }

                handleToPathMap.put(handle, path);
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
        return handleToPathMap.get(handle);
    }

    /**
     * Load the record identified by the path.
     *
     * @param path uniquely identifying the record to be loaded.
     * @return the loaded record, or null if no record could be found.
     */
    public synchronized Record load(String path)
    {
        checkPath(path);
        
        String[] elements = PathUtils.getPathElements(path);
        return load(elements);
    }

    private Record load(String[] elements)
    {
        Record record = baseRecord;
        for(String pathElement: elements)
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

    /**
     * Loads all records whose paths match the given path.  The path may
     * include wildcards.
     *
     * @param path    search path to use, may include wildcards
     * @param records filled with a mapping from path to record for all
     *                records that are stored at a path matching the search
     *                path
     */
    public synchronized void loadAll(String path, Map<String, Record> records)
    {
        checkPath(path);
        
        loadAll(baseRecord, PathUtils.getPathElements(path), 0, "", records);
    }

    private void loadAll(Record record, String[] elements, int pathIndex, String resolvedPath, Map<String, Record> records)
    {
        if(pathIndex == elements.length)
        {
            records.put(resolvedPath, record);
            return;
        }

        for(String key: record.keySet())
        {
            if(PathUtils.elementMatches(elements[pathIndex], key))
            {
                loadAll((Record) record.get(key), elements, pathIndex + 1, PathUtils.getPath(resolvedPath, key), records);
            }
        }
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
        
        return getRecord(PathUtils.getPathElements(path)) != null;
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
        MutableRecord record = newRecord.copy(true);
        allocateHandles(record);

        return store(path, record);
    }

    private void allocateHandles(MutableRecord record)
    {
        record.setHandle(allocateHandle());
        for(Object child: record.values())
        {
            if(child instanceof MutableRecord)
            {
                allocateHandles((MutableRecord) child);
            }
        }
    }

    private Record store(String path, MutableRecord record)
    {
        checkPath(path);

        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }

        MutableRecord parent = getRecord(PathUtils.getParentPathElements(pathElements));
        if(parent == null)
        {
            throw new IllegalArgumentException("No parent record for path '" + path + "'");
        }

        // Save first before hooking up in memory
        recordSerialiser.serialise(path, record, true);
        parent.put(pathElements[pathElements.length - 1], record);
        handleToPathMap.put(record.getHandle(), path);
        return record;
    }

    private void checkPath(String path)
    {
        if (path == null || path.equals(""))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
    }

    private MutableRecord getRecord(String[] pathElements)
    {
        MutableRecord record = baseRecord;
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
        
        String[] parentElements = PathUtils.getParentPathElements(path);
        String baseName = PathUtils.getBaseName(path);

        MutableRecord parentRecord = getRecord(parentElements);
        if(parentRecord == null)
        {
            throw new IllegalArgumentException("No parent record for path '" + path + "'" );
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
        for(String key: values.simpleKeySet())
        {
            copy.put(key, values.get(key));
        }

        // Remove simple values not present in the input
        for(String key: record.simpleKeySet())
        {
            if(values.get(key) == null)
            {
                copy.remove(key);
            }
        }

        parentRecord.put(baseName, copy);
        recordSerialiser.serialise(path, copy, false);
        return copy;
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
        if(containsRecord(path))
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

        MutableRecord parentRecord = getRecord(PathUtils.getParentPathElements(path));
        if (parentRecord == null)
        {
            throw new IllegalArgumentException("No parent record for path '" + path + "'");
        }

        String baseName = PathUtils.getBaseName(path);
        Object value = parentRecord.get(baseName);
        if(value != null && value instanceof Record)
        {
            Record result = (Record) parentRecord.remove(baseName);
            handleToPathMap.remove(result.getHandle());
            recordSerialiser.delete(path);
            return result;
        }
        return null;
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
        MutableRecord record = (MutableRecord) load(sourcePath);
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
            record = store(destinationPath, (MutableRecord) record);
        }
        return record;
    }

    public void setRecordSerialiser(RecordSerialiser recordSerialiser)
    {
        this.recordSerialiser = recordSerialiser;
    }
}
