package com.zutubi.pulse.master.model;

import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

/**
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
    private AccessManager accessManager;

    public BuildColumns(String columns, AccessManager accessManager)
    {
        this.columns = columns.split(",");
        this.accessManager = accessManager;
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
                return 6;
            }
            else
            {
                if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_VIEW_SOURCE, project))
                {
                    return 6;
                }
                else
                {
                    return 5;
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
        return new String[] { KEY_ACTIONS, KEY_COMPLETED, KEY_ELAPSED, KEY_ID, KEY_OWNER, KEY_PROJECT, KEY_REASON, KEY_REVISION, KEY_STATUS, KEY_TESTS, KEY_VERSION, KEY_WHEN, KEY_WARNINGS };
    }

    public void removeAll(final String... keys)
    {
        columns = CollectionUtils.filterToArray(columns,  new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                for(String key: keys)
                {
                    if(s.equals(key))
                    {
                        return false;
                    }
                }

                return true;
            }
        });
    }
}
