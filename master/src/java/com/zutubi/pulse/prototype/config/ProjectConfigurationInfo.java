package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.table.TableDefinition;
import com.zutubi.prototype.table.ActionDefinition;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class ProjectConfigurationInfo implements TableDefinition, ActionDefinition
{
    private static final List<String> columns = new LinkedList<String>();
    static
    {
        columns.add("name");        
    }

    public List<String> getColumns()
    {
        return columns;
    }

    public Class getActionHandler()
    {
        return ProjectConfigurationActionHandler.class;
    }
}
