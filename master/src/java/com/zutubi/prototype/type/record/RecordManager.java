package com.zutubi.prototype.type.record;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;

/**
 *
 *
 */
public class RecordManager
{
    /**
     * The base record is the 'anchor' point for all of the records held in memory. All searches for
     * records start from here.
     */
    private MutableRecord baseRecord = new MutableRecordImpl();
    private MasterConfigurationManager configurationManager;
    private RecordSerialiser recordSerialiser;

    public void init()
    {
        recordSerialiser = new DefaultRecordSerialiser(configurationManager.getUserPaths().getRecordRoot());
        baseRecord = recordSerialiser.deserialise("");
    }

    /**
     * Load the record identified by the path.
     *
     * @param path uniquely identifying the record to be loaded.
     * @return the loaded record, or null if no record could be found.
     */
    public Record load(String path)
    {
        Record record = baseRecord;
        for(String pathElement: PathUtils.getPathElements(path))
        {
            Object data = record.get(pathElement);
            if (data == null || !(data instanceof MutableRecordImpl))
            {
                return null;
            }
            record = (Record) record.get(pathElement);
        }
        return record;
    }

    /**
     * Returns true if a record exists at the specified path.
     *
     * @param path uniquely identifying a record.
     * @return true if a record exists, false otherwise.
     */
    public boolean containsRecord(String path)
    {
        MutableRecord record = baseRecord;
        for(String pathElement: PathUtils.getPathElements(path))
        {
            Object data = record.get(pathElement);
            if (data == null || !(data instanceof MutableRecordImpl))
            {
                return false;
            }
            record = (MutableRecordImpl) record.get(pathElement);
        }
        return record != null;
    }


    public void insert(String path, Record newRecord)
    {
        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }

        MutableRecord parent = getRecord(PathUtils.getParentPathElements(pathElements));

        // Save first before hooking up in memory
        recordSerialiser.serialise(path, (MutableRecord) newRecord, true);
        parent.put(pathElements[pathElements.length - 1], newRecord.createMutable());
    }

    private MutableRecord getRecord(String[] pathElements)
    {
        MutableRecord record = baseRecord;
        for (String element : pathElements)
        {
            if (record.get(element) == null)
            {
                record.put(element, new MutableRecordImpl());
            }
            Object obj = record.get(element);
            if (!(obj instanceof MutableRecordImpl))
            {
                throw new IllegalArgumentException("Invalid path '" + PathUtils.getPath(pathElements) + "'");
            }
            record = (MutableRecordImpl) obj;
        }

        return record;
    }

    public void store(String path, Record values)
    {
        MutableRecord record = getRecord(PathUtils.getPathElements(path));
        record.update(values);
        recordSerialiser.serialise(path, record, false);
    }

    /**
     * Delete the record at the specified path.
     *
     * @param path identifying the record to be deleted.
     * @return an instance of the record just deleted, or null if no record exists at the specified path.
     */
    public Record delete(String path)
    {
        MutableRecord record = baseRecord;
        for(String pathElement: PathUtils.getParentPathElements(path))
        {
            if (!record.containsKey(pathElement))
            {
                return null;
            }
            Object obj = record.get(pathElement);
            if (!(obj instanceof Record))
            {
                return null;
            }

            record = (MutableRecordImpl) record.get(pathElement);
        }

        String basePath = PathUtils.getBasePath(path);
        if (record.containsKey(basePath))
        {
            recordSerialiser.delete(path);
            Object obj = record.get(basePath);
            if (obj instanceof MutableRecordImpl)
            {
                return (Record) record.remove(basePath);
            }
        }
        return null;
    }

    /**
     * Copy the record contents from the source path to the destination path
     *
     * @param sourcePath      path to copy from
     * @param destinationPath path to copy to
     */
    public void copy(String sourcePath, String destinationPath)
    {
        MutableRecord record = (MutableRecord) load(sourcePath);
        if (record != null)
        {
            MutableRecord copy = record.createMutable();
            insert(destinationPath, copy);
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
