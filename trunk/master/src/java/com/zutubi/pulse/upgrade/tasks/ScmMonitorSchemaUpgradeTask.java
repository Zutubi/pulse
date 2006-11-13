package com.zutubi.pulse.upgrade.tasks;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class ScmMonitorSchemaUpgradeTask extends MigrateSchemaUpgradeTask
{
    public ScmMonitorSchemaUpgradeTask()
    {
        List<String> mappings = new LinkedList<String>();
        mappings.add("com/zutubi/pulse/upgrade/schema/build_1041/Scm.hbm.xml");
        setMappings(mappings);
    }
}
