package com.zutubi.pulse.prototype.record;

import java.util.StringTokenizer;

/**
 * 
 */
public class DefaultRecordManager implements RecordManager
{
    private Record baseRecord = new SingleRecord("root");
    
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
        Record record = load(path);
        if(record == null)
        {
            // create the record.
            record = baseRecord;        
            StringTokenizer tokens = new StringTokenizer(path, "/", false);
            while (tokens.hasMoreTokens())
            {
                String pathElement = tokens.nextToken();
                if (record.get(pathElement) == null)
                {
                    SingleRecord r;
                    if (tokens.hasMoreTokens())
                    {
                        r = new SingleRecord("");
                    }
                    else
                    {
                        r = new SingleRecord(newRecord.getSymbolicName());
                    }
                    record.put(pathElement, r);
                }
                record = (Record) record.get(pathElement);
            }
        }

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
}
