package com.cinnamonbob.bootstrap.config;

import com.cinnamonbob.core.util.IOUtils;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class FileConfigurationTest extends TestCase
{
    private Configuration config = null;
    private File testProperties;

    public FileConfigurationTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // temporary properties file
        testProperties = File.createTempFile(FileConfigurationTest.class.getName(), ".properties");

        Properties defaults = new Properties();
        defaults.put("key", "value");
        IOUtils.write(defaults, testProperties);

        // add setup code here.
        config = new FileConfiguration(testProperties);
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        if (!testProperties.delete())
        {
            throw new IOException("");
        }

        config = null;

        super.tearDown();
    }

    public void testGetProperties() throws Exception
    {
        assertEquals("value", config.getProperty("key"));
    }

    public void testSetProperties() throws Exception
    {
        config.setProperty("key", "anotherValue");
        assertEquals("anotherValue", config.getProperty("key"));

        Properties props = IOUtils.read(testProperties);
        assertEquals("anotherValue", props.getProperty("key"));
        assertEquals(1, props.size());
    }

    public void testModifyPropertiesFileDirectly() throws IOException, InterruptedException
    {
        assertEquals("value", config.getProperty("key"));
        Thread.sleep(1000);
        Properties updatedProperties = new Properties();
        updatedProperties.put("key", "anotherValue");
        IOUtils.write(updatedProperties, testProperties);
        assertEquals("anotherValue", config.getProperty("key"));
    }
}