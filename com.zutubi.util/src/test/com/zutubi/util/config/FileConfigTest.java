package com.zutubi.util.config;

import com.zutubi.util.io.IOUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class FileConfigTest extends TestCase
{
    private Config config = null;
    private File testProperties;

    public FileConfigTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // temporary properties file
        testProperties = File.createTempFile(FileConfigTest.class.getName(), ".properties");

        Properties defaults = new Properties();
        defaults.put("key", "value");
        IOUtils.write(defaults, testProperties);

        // add setup code here.
        config = new FileConfig(testProperties);
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
        Assert.assertEquals("value", config.getProperty("key"));
    }

    public void testSetProperties() throws Exception
    {
        config.setProperty("key", "anotherValue");
        Assert.assertEquals("anotherValue", config.getProperty("key"));

        Properties props = IOUtils.read(testProperties);
        Assert.assertEquals("anotherValue", props.getProperty("key"));
        Assert.assertEquals(1, props.size());
    }

    public void testCreationOfPropertiesFile() throws Exception
    {
        Assert.assertTrue(testProperties.delete());

        config.setProperty("key", "anotherValue");
        Assert.assertEquals("anotherValue", config.getProperty("key"));

        Properties props = IOUtils.read(testProperties);
        Assert.assertEquals("anotherValue", props.getProperty("key"));
        Assert.assertEquals(1, props.size());
    }
}
