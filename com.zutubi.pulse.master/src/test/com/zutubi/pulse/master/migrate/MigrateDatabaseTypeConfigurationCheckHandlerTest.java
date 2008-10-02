package com.zutubi.pulse.master.migrate;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.tove.config.setup.DatabaseType;

/**
 *
 *
 */
public class MigrateDatabaseTypeConfigurationCheckHandlerTest extends PulseTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testEmbeddedDatabaseConfiguration() throws Exception
    {
        MigrateDatabaseTypeConfiguration embedded = new MigrateDatabaseTypeConfiguration();
        embedded.setType(DatabaseType.EMBEDDED);

        MigrateDatabaseTypeConfigurationCheckHandler checkHandler = new MigrateDatabaseTypeConfigurationCheckHandler();
        try
        {
            checkHandler.test(embedded);
        }
        catch (Exception e)
        {
            fail("Unexpected exception.");
        }
    }

    public void testNonEmbeddedRegisteredDriver()
    {

    }

    public void testNonEmbeddedNonRegisteredDriver()
    {
        
    }
}
