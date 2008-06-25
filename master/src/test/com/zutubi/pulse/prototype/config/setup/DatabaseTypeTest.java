package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.pulse.test.PulseTestCase;

import java.util.Properties;

/**
 *
 *
 */
public class DatabaseTypeTest extends PulseTestCase
{
    public void testMysqlConfigPropertiesConversion()
    {
        DatabaseType type = DatabaseType.MYSQL;

        SetupDatabaseTypeConfiguration config = new SetupDatabaseTypeConfiguration();
        config.setHost("mysqlhost");
        config.setPort(12345);
        config.setDatabase("mysqldatabase");
        config.setUser("mysqluser");
        config.setPassword("mysqlpass");

        Properties props = type.getDatabaseProperties(config);

        MigrateDatabaseTypeConfiguration config2 = type.getDatabaseConfiguration(props);

        assertEquals(config.getHost(), config2.getHost());
        assertEquals(config.getPort(), config2.getPort());
        assertEquals(config.getDatabase(), config2.getDatabase());
        assertEquals(config.getUser(), config2.getUser());
        assertEquals(config.getPassword(), config2.getPassword());
    }
}
