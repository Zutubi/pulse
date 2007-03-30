package com.zutubi.prototype.type.record;

import java.util.StringTokenizer;

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
    private MutableRecordImpl baseRecord = new MutableRecordImpl();

    /**
     * The path separator used for paths that identify records.
     */
    private static final String PATH_SEPARATOR = "/";

    /**
     * Load the record identified by the path.
     *
     * @param path uniquely identifying the record to be loaded.
     * @return the loaded record, or null if no record could be found.
     */
    public Record load(String path)
    {
        Record record = baseRecord;
        StringTokenizer tokens = new StringTokenizer(path, PATH_SEPARATOR, false);
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
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
        MutableRecordImpl record = baseRecord;
        StringTokenizer tokens = new StringTokenizer(path, PATH_SEPARATOR, false);
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
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

        Record parent = getRecord(PathUtils.getParentPathElements(pathElements));
        try
        {
            parent.put(pathElements[pathElements.length - 1], newRecord.clone());
        }
        catch (CloneNotSupportedException e)
        {
            // Buh
        }
    }

    private MutableRecordImpl getRecord(String[] pathElements)
    {
        MutableRecordImpl record = baseRecord;
        for (int i = 0; i < pathElements.length; i++)
        {
            String element = pathElements[i];
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
        MutableRecordImpl record = getRecord(PathUtils.getPathElements(path));
        record.update((MutableRecordImpl) values);
    }

    /**
     * Delete the record at the specified path.
     *
     * @param path identifying the record to be deleted.
     * @return an instance of the record just deleted, or null if no record exists at the specified path.
     */
    public Record delete(String path)
    {
        MutableRecordImpl record = baseRecord;
        StringTokenizer tokens = new StringTokenizer(path, PATH_SEPARATOR, false);

        String pathElement = null;
        while (tokens.hasMoreTokens())
        {
            pathElement = tokens.nextToken();

            // search for the record until we have no more tokens. At this point, we
            // are are at the parent record with the key for the record we are interested in.
            if (!tokens.hasMoreTokens())
            {
                break;
            }
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

        if (record.containsKey(pathElement))
        {
            Object obj = record.get(pathElement);
            if (obj instanceof MutableRecordImpl)
            {
                return (Record) record.remove(pathElement);
            }
        }
        return null;
    }

    /**
     * Copy the record contents from the source path to the destination path
     *
     * @param sourcePath
     * @param destinationPath
     */
    public void copy(String sourcePath, String destinationPath)
    {
        try
        {
            MutableRecordImpl record = (MutableRecordImpl) load(sourcePath);
            if (record != null)
            {
                MutableRecordImpl copy = (MutableRecordImpl) record.clone();
                insert(destinationPath, copy);
            }
        }
        catch (CloneNotSupportedException e)
        {
            // Will not happen since record is cloneable.
            e.printStackTrace();
        }
    }
}
