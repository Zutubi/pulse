package com.zutubi.pulse.prototype.record;

import java.util.StringTokenizer;
import java.util.List;
import java.util.LinkedList;

/**
 * @deprecated
 */
public class InMemoryRecordManager implements RecordManager
{
    // default in memory implementation.

    /**
     * Base record used as the root of the object store.
     */
    private Record baseRecord = new SingleRecord("root");

    /**
     * Load the record described by the path, or null if no record exists.
     *
     * @param path uniquely identifying the record.
     *
     * @return the specified record, or null if no record exists.
     */
    public Record load(String path)
    {
        Record record = baseRecord;
        
        List<String> pathElements = getPathElements(path);
        for (String pathElement : pathElements)
        {
            if (!record.containsKey(pathElement))
            {
                return null;
            }

            Object data = record.get(pathElement);
            // we can not reference raw data, only records.
            if (!(data instanceof Record))
            {
                throw new IllegalArgumentException("Invalid path: '"+path+"', references object of type: " +
                        data.getClass().getName());
            }
            
            record = (Record) record.get(pathElement);
        }
        return record;
    }

    /**
     * Store the specified records data at the given path.
     *
     * @param path uniquely identifying the record path.
     *
     * @param newRecord represents the new data.
     */
    public void store(String path, Record newRecord)
    {
        Record record = baseRecord;

        // locate the record.
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
            if (record.get(pathElement) == null)
            {
                // do not have the intermediate path element, so create it.
                if (tokens.hasMoreTokens())
                {
                    record.put(pathElement, new SingleRecord(""));
                }
                else
                {
                    // this is the last one, so ensure that we correctly add the symbolic name.
                    record.put(pathElement, new SingleRecord(newRecord.getSymbolicName()));
                }
            }
            record = (Record) record.get(pathElement);
        }

        // the record is the entry we want. Lets update it.
        record.putAll(newRecord);
    }

    public Record delete(String path)
    {
        Record record = baseRecord;

        StringTokenizer tokens = new StringTokenizer(path, "/", false);

        Record parent = null;
        String pathElement = null;
        while (tokens.hasMoreTokens())
        {
            pathElement = tokens.nextToken();
            parent = record;
            record = (Record) record.get(pathElement);
        }
        
        return (Record) parent.remove(pathElement);
    }

    private List<String> getPathElements(String path)
    {
        List<String> pathElements = new LinkedList<String>();
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        while (tokens.hasMoreTokens())
        {
            pathElements.add(tokens.nextToken());
        }
        return pathElements;
    }
}
