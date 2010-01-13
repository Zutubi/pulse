package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

public class OptionalRenameTriggerIntervalColumnUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    private OptionalRenameTriggerIntervalColumnUpgradeTask task;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        task = new OptionalRenameTriggerIntervalColumnUpgradeTask();
        task.setDataSource(dataSource);
        task.setHibernateProperties(databaseConfig.getHibernateProperties());
        task.setMappings(getMappings());
    }

    public void testUpgradeIsOptional() throws Exception
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            task.execute(con);
            assertFalse(task.hasFailed());
            task.execute(con);
            assertFalse(task.hasFailed());
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    protected List<String> getMappings()
    {
        return Arrays.asList("com/zutubi/pulse/master/upgrade/tasks/OptionalRenameTriggerIntervalColumnUpgradeTaskTest-schema.hbm.xml");
    }
}
