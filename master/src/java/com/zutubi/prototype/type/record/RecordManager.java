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
    private Record baseRecord = new Record();

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
        try
        {
            Record record = baseRecord;
            StringTokenizer tokens = new StringTokenizer(path, PATH_SEPARATOR, false);
            while (tokens.hasMoreTokens())
            {
                String pathElement = tokens.nextToken();
                Object data = record.get(pathElement);
                if (data == null || !(data instanceof Record))
                {
                    return null;
                }
                record = (Record) record.get(pathElement);
            }
            return record.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // should not happen, record implements cloneable.
            e.printStackTrace();
            return null;
        }
    }

    public boolean containsRecord(String path)
    {
        Record record = baseRecord;
        StringTokenizer tokens = new StringTokenizer(path, PATH_SEPARATOR, false);
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
            Object data = record.get(pathElement);
            if (data == null || !(data instanceof Record))
            {
                return false;
            }
            record = (Record) record.get(pathElement);
        }
        return record != null;
    }

    /**
     * Store the provided record at the requested path. If a record already exists at this location,
     * the two records are merged.
     * 
     * @param path
     * @param newRecord
     */
    public void store(String path, Record newRecord)
    {
        Record record = baseRecord;
        StringTokenizer tokens = new StringTokenizer(path, PATH_SEPARATOR, false);
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
            // if a record in the path does not exist, create it. An empty record is fine. 
            if (record.get(pathElement) == null)
            {
                record.put(pathElement, new Record());
            }
            Object obj = record.get(pathElement);
            if (!(obj instanceof Record))
            {
                // ok, problem. We have a non-record entry, meaning that we are inside
                // a record that contains data.
                throw new IllegalArgumentException("Invalid path.");
            }
            record = (Record) record.get(pathElement);
        }
        record.putAll(newRecord);
    }

    public Record delete(String path)
    {
        Record record = baseRecord;
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
            
            record = (Record) record.get(pathElement);
        }

        if (record.containsKey(pathElement))
        {
            Object obj = record.get(pathElement);
            if (obj instanceof Record)
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
            Record record = load(sourcePath);
            if (record != null)
            {
                Record copy = record.clone();
                store(destinationPath, copy);
            }
        }
        catch (CloneNotSupportedException e)
        {
            // Will not happen since record is cloneable.
            e.printStackTrace();
        }
    }
}
