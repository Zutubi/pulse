package com.zutubi.pulse.prototype.config.user.contacts;

import com.zutubi.prototype.table.TableDefinition;

import java.util.List;
import java.util.Arrays;

/**
 *
 *
 */
public class ContactConfigurationInfo implements TableDefinition
{
    public List<String> getColumns()
    {
        return Arrays.asList("name", "uid");
    }
}
