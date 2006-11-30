package com.zutubi.pulse.model;

/**
 */
public class BuildColumns
{
    public static final String KEY_ID = "id";
    public static final String KEY_PROJECT = "project";
    public static final String KEY_SPECIFICATION = "spec";
    public static final String KEY_STATUS = "status";
    public static final String KEY_REASON = "reason";
    public static final String KEY_TESTS = "tests";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_REVISION = "rev";
    public static final String KEY_VERSION = "version";
    public static final String KEY_WHEN = "when";
    public static final String KEY_ELAPSED = "elapsed";
    public static final String KEY_ACTIONS = "actions";
    public static final String KEY_WARNINGS = "warnings";
    public static final String KEY_ERRORS = "errors";

    private String[] columns;
    private ProjectManager projectManager;

    public BuildColumns(String columns, ProjectManager projectManager)
    {
        this.columns = columns.split(",");
        this.projectManager = projectManager;
    }

    public String[] getColumns()
    {
        return columns;
    }

    public int getSpan(String key)
    {
        return getSpan(key, null);
    }

    public int getSpan(String key, Project project)
    {
        if (key.equals(KEY_STATUS))
        {
            return 2;
        }
        else if (key.equals(KEY_ACTIONS))
        {
            if (project == null)
            {
                return 5;
            }
            else
            {
                try
                {
                    projectManager.checkWrite(project);
                    return 5;
                }
                catch (Exception e)
                {
                    return 4;
                }
            }
        }
        else
        {
            return 1;
        }
    }

    public int getTotalSpan()
    {
        return getTotalSpan(null);
    }

    public int getTotalSpan(Project project)
    {
        int total = 0;

        for (String key : columns)
        {
            total += getSpan(key, project);
        }

        return total;
    }

    public static String[] getAllColumns()
    {
        return new String[] { KEY_ACTIONS, KEY_ELAPSED, KEY_ID, KEY_OWNER, KEY_PROJECT, KEY_REASON, KEY_REVISION, KEY_SPECIFICATION, KEY_STATUS, KEY_TESTS, KEY_VERSION, KEY_WHEN, KEY_WARNINGS };
    }
}
