package com.zutubi.prototype.type.record;

import java.util.StringTokenizer;

/**
 *
 *
 */
public class RecordManager
{
    private Record baseRecord = new Record();

    public Record load(String path)
    {
        Record record = baseRecord;
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
            Object data = record.get(pathElement);
            if (data == null)
            {
                return null;
            }
            if (!(data instanceof Record))
            {
                throw new IllegalArgumentException("Invalid path: '"+path+"'");
            }
            record = (Record) record.get(pathElement);
        }
        return record;
    }

    public void store(String path, Record newRecord)
    {
        Record record = baseRecord;
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
            if (record.get(pathElement) == null)
            {
                record.put(pathElement, new Record());
            }
            record = (Record) record.get(pathElement);
        }
        record.putAll(newRecord);
    }

    public Object delete(String path)
    {
        Record record = baseRecord;
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        
        String pathElement = null;
        while (tokens.hasMoreTokens())
        {
            pathElement = tokens.nextToken();
            if (!tokens.hasMoreTokens())
            {
                break;                
            }
            record = (Record) record.get(pathElement);
        }
        return record.remove(pathElement);
    }
}
