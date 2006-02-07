package com.cinnamonbob.bootstrap;

import junit.framework.TestCase;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class ReadWriteConfigurationTest extends TestCase
{
    private Properties defaultProperties;
    private Properties properties;

    private ReadWriteConfiguration config;

    public ReadWriteConfigurationTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        defaultProperties = new Properties();
        properties = new Properties();
        config = new ReadWriteConfiguration(defaultProperties, properties);
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        config = null;

        super.tearDown();
    }

    public void testReadOnly()
    {
        defaultProperties.put("test.property", "blah");

        assertEquals("blah", config.getProperty("test.property"));
        assertTrue(config.hasProperty("test.property"));

        assertNull(config.getProperty("does.not.exist"));
        assertFalse(config.hasProperty("does.not.exist"));
    }

    public void testReadWrite()
    {
        assertFalse(config.hasProperty("property"));
        assertNull(config.getProperty("property"));

        config.setProperty("property", "value");

        assertEquals("value", properties.getProperty("property"));
        assertNull(defaultProperties.getProperty("property"));

        assertEquals("value", config.getProperty("property"));
        assertTrue(config.hasProperty("property"));
    }

    public void testResetDefaults()
    {
        config.setProperty("property", "value");
        assertTrue(config.hasProperty("property"));
        config.resetDefaults();
        assertFalse(config.hasProperty("property"));
    }
}