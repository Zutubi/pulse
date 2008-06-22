package com.zutubi.pulse.database;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.DriverWrapper;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

/**
 *
 *
 */
public class DriverRegistryTest extends PulseTestCase
{
    private File tmp;
    private DriverRegistry registry;
    private static final String EMBEDDED_DRIVER = "org.hsqldb.jdbcDriver";

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        registry = new DriverRegistry();
        registry.setDriverDir(tmp);
        registry.init();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        registry = null;

        unregisterAllDriversFromDriverManager();

        super.tearDown();
    }

    private void unregisterAllDriversFromDriverManager() throws SQLException
    {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements())
        {
            DriverManager.deregisterDriver(drivers.nextElement());    
        }
    }

    public void testRegisterEmbeddedDriver() throws IOException
    {
        registry.register(EMBEDDED_DRIVER);

        assertTrue(registry.isRegistered(EMBEDDED_DRIVER));
    }

    public void testRegistryFile() throws IOException
    {
        registry.register(EMBEDDED_DRIVER);

        // check file is there.
        File expectedRegistryFile = new File(tmp, ".registry");
        assertTrue(expectedRegistryFile.isFile());

        Properties registryFileContents = IOUtils.read(expectedRegistryFile);
        assertTrue(registryFileContents.containsKey(EMBEDDED_DRIVER));
    }

    public void testBadDriver() throws IOException
    {
        try
        {
            registry.register("org.unknown.jdbcDriver");
            fail();
        }
        catch (IOException e)
        {
            // expected.
            e.printStackTrace();
        }

    }

    public void testInit() throws IOException, SQLException
    {
        registry.register(EMBEDDED_DRIVER);
        assertTrue(registry.isRegistered(EMBEDDED_DRIVER));

        unregisterAllDriversFromDriverManager();

        registry = new DriverRegistry();
        registry.setDriverDir(tmp);
        registry.init();
        assertTrue(registry.isRegistered(EMBEDDED_DRIVER));

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        assertTrue(drivers.hasMoreElements());

        Driver driver = drivers.nextElement();
        assertTrue(driver instanceof DriverWrapper);
        assertEquals(EMBEDDED_DRIVER, ((DriverWrapper)driver).getDelegate().getClass().getCanonicalName());
    }

    public void testUnregisterOnRestart() throws IOException, SQLException
    {
        registry.register(EMBEDDED_DRIVER);
        assertTrue(registry.isRegistered(EMBEDDED_DRIVER));

        registry.unregisterOnRestart(EMBEDDED_DRIVER);
        assertTrue(registry.isRegistered(EMBEDDED_DRIVER));

        unregisterAllDriversFromDriverManager();

        registry = new DriverRegistry();
        registry.setDriverDir(tmp);
        registry.init();
        
        assertFalse(registry.isRegistered(EMBEDDED_DRIVER));
        assertFalse(DriverManager.getDrivers().hasMoreElements());
    }
}
