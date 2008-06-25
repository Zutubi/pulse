package com.zutubi.pulse.migrate;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.monitor.JobManager;

import java.util.Properties;
import java.io.IOException;

/**
 *
 *
 */
public class MigrationManagerTest extends PulseTestCase
{
    private MigrationManager migrationManager;

    private JobManager jobManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        System.clearProperty(MigrationManager.REQUEST_MIGRATE_SYSTEM_KEY);

        jobManager = new JobManager();
        
        migrationManager = new MigrationManager();
        migrationManager.setJobManager(jobManager);
/*
        migrationManager.setDatabaseConfig();
        migrationManager.setUserPaths();
*/
    }

    protected void tearDown() throws Exception
    {
        System.clearProperty(MigrationManager.REQUEST_MIGRATE_SYSTEM_KEY);

        super.tearDown();
    }

    public void testIsRequested()
    {
        assertFalse(migrationManager.isRequested());

        System.setProperty(MigrationManager.REQUEST_MIGRATE_SYSTEM_KEY, Boolean.toString(true));

        assertTrue(migrationManager.isRequested());
    }

/*
    public void testScheduleMigration() throws IOException
    {
        assertEquals(0, jobManager.getJobKeys().size());

        migrationManager.scheduleMigration(new Properties());

        assertEquals(1, jobManager.getJobKeys().size());
        assertNotNull(jobManager.getJob(MigrationManager.MIGRATE_JOB_KEY));
    }
*/


}
