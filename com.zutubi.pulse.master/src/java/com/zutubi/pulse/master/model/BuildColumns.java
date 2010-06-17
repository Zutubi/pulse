package com.zutubi.pulse.master.model;

/**
 * Utility class exposing details of build columns to display in the web UI.
 */
public class BuildColumns
{
    public static final String KEY_ID = "id";
    public static final String KEY_PROJECT = "project";
    public static final String KEY_STATUS = "status";
    public static final String KEY_REASON = "reason";
    public static final String KEY_TESTS = "tests";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_REVISION = "rev";
    public static final String KEY_VERSION = "version";
    public static final String KEY_WHEN = "when";
    public static final String KEY_ELAPSED = "elapsed";
    public static final String KEY_COMPLETED = "completed";
    public static final String KEY_ACTIONS = "actions";
    public static final String KEY_WARNINGS = "warnings";
    public static final String KEY_ERRORS = "errors";

    private String[] columns;

    public BuildColumns(String columns)
    {
        this.columns = columns.split(",");
    }

    public int getCount()
    {
        return columns.length;
    }
    
    public String[] getColumns()
    {
        return columns;
    }

    public int getSpan(String key)
    {
        if (key.equals(KEY_STATUS))
        {
            return 2;
        }
        else if (key.equals(KEY_ACTIONS))
        {
            return 5;
        }
        else
        {
            return 1;
        }
    }

    public int getTotalSpan()
    {
        int total = 0;

        for (String key : columns)
        {
            total += getSpan(key);
        }

        return total;
    }

    public static String[] getAllColumns()
    {
        return new String[] { KEY_ACTIONS, KEY_COMPLETED, KEY_ELAPSED, KEY_ID, KEY_OWNER, KEY_PROJECT, KEY_REASON, KEY_REVISION, KEY_STATUS, KEY_TESTS, KEY_VERSION, KEY_WHEN, KEY_WARNINGS };
    }
}
