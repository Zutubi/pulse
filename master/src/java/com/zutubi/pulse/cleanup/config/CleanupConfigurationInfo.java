package com.zutubi.pulse.cleanup.config;

import com.zutubi.prototype.table.TableDefinition;

import java.util.List;
import java.util.Arrays;

/**
 *
 *
 */
public class CleanupConfigurationInfo implements TableDefinition
{
    public List<String> getColumns()
    {
        return Arrays.asList("name", "states", "when");
    }
}
