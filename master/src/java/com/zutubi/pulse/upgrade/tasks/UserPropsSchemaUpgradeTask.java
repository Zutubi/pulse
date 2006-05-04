/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
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
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/User.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/Project.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/BuildSpecification.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/Slave.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/BuildResult.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/ChangeList.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/CommandResult.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/Feature.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/Revision.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/Scm.hbm.xml");
        mappings.add("com/zutubi/pulse/upgrade/schema/build-1020/StoredArtifact.hbm.xml");
        setMappings(mappings);
    }
}
