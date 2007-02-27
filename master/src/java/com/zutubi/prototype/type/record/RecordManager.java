package com.zutubi.prototype.type.record;

import java.util.StringTokenizer;
import java.util.Map;

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
    private MutableRecord baseRecord = new MutableRecord();

    /**
     * The path separator used for paths that identify records.
     */
    private static final String PATH_SEPARATOR = "/";

    /**
     * Load the record identified by the path.
     *
     * @param path uniquely identifying the record to be loaded.
     *
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
            if (data == null || !(data instanceof MutableRecord))
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
     *
     * @return true if a record exists, false otherwise.
     */
    public boolean containsRecord(String path)
    {
        MutableRecord record = baseRecord;
        StringTokenizer tokens = new StringTokenizer(path, PATH_SEPARATOR, false);
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
            Object data = record.get(pathElement);
            if (data == null || !(data instanceof MutableRecord))
            {
                return false;
            }
            record = (MutableRecord) record.get(pathElement);
        }
        return record != null;
    }

    
    public void insert(String path, MutableRecord newRecord)
    {
        String[] pathElements = PathUtils.getPathElements(path);
        if(pathElements == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }

        Record parent = getRecord(PathUtils.getParentPathElements(pathElements));
        parent.put(pathElements[pathElements.length - 1], newRecord);
    }

    private MutableRecord getRecord(String[] pathElements)
    {
        MutableRecord record = baseRecord;
        for(int i = 0; i < pathElements.length - 1; i++)
        {
            String element = pathElements[i];
            if(record.get(element) == null)
            {
                record.put(element, new MutableRecord());
            }
            Object obj = record.get(element);
            if(!(obj instanceof MutableRecord))
            {
                throw new IllegalArgumentException("Invalid path '" + PathUtils.getPath(pathElements) + "'");
            }
            record = (MutableRecord) obj;
        }

        return record;
    }

    /**
     * Updates the record at the requested path.  A record must exist at the
     * given path.  To create new records, use #insert(String, MutableRecord).
     *
     * @see #insert(String, MutableRecord)
     *
     * @param path identifying the path of the existing record.
     * @param values new primitive values to write to the record
     */
    public void store(String path, Map<String, String> values)
    {
        MutableRecord record = getRecord(PathUtils.getPathElements(path));
        record.putAll(values);
    }

    /**
     * Delete the record at the specified path.
     *
     * @param path identifying the record to be deleted.
     *
     * @return an instance of the record just deleted, or null if no record exists at the specified path.
     */
    public Record delete(String path)
    {
        MutableRecord record = baseRecord;
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
            if (!(obj instanceof MutableRecord))
            {
                return null;
            }
            
            record = (MutableRecord) record.get(pathElement);
        }

        if (record.containsKey(pathElement))
        {
            Object obj = record.get(pathElement);
            if (obj instanceof MutableRecord)
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
            MutableRecord record = (MutableRecord) load(sourcePath);
            if (record != null)
            {
                MutableRecord copy = (MutableRecord) record.clone();
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
