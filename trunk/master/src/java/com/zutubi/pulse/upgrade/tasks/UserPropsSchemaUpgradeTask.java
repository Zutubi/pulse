package com.zutubi.pulse.upgrade.tasks;

import java.util.List;
import java.util.LinkedList;

/**
 * A simple preconfigured MigrateSchemaUpgradeTask for the user changes in build 1020.
 *
 * @author Daniel Ostermeier
 */
public class UserPropsSchemaUpgradeTask extends MigrateSchemaUpgradeTask
{
    public UserPropsSchemaUpgradeTask()
    {
        List<String> mappings = new LinkedList<String>();
        mappings.add("com/zutubi/pulse/upgrade/schema/build_1011/User.hbm.xml");
        setMappings(mappings);
    }
}
