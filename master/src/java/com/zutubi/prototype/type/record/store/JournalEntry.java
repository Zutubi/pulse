package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public class JournalEntry
{
    private int id;

    private String action;
    private String path;
    private Record record;

    public JournalEntry(String action, String path)
    {
        this(action, path, null);
    }

    public JournalEntry(String action, String path, Record record)
    {
        this.action = action;
        this.path = path;
        this.record = record;
    }

    public String getAction()
    {
        return action;
    }

    public String getPath()
    {
        return path;
    }

    public Record getRecord()
    {
        return record;
    }

    int getId()
    {
        return id;
    }

    void setId(int id)
    {
        this.id = id;
    }
}
