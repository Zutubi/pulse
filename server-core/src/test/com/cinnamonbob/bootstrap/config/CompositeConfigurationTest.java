package com.cinnamonbob.bootstrap.config;

import junit.framework.*;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class CompositeConfigurationTest extends TestCase
{

    public CompositeConfigurationTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

        super.tearDown();
    }

    public void testTwoWritableConfigs()
    {
        Properties propsA = new Properties();
        Properties propsB = new Properties();

        Configuration configA = new PropertiesConfiguration(propsA);
        Configuration configB = new PropertiesConfiguration(propsB);

        Configuration composite = new CompositeConfiguration(configA, configB);
        assertFalse(composite.hasProperty("key"));

        propsB.put("key", "value");
        assertEquals("value", composite.getProperty("key"));
        propsA.put("key", "anotherValue");
        assertEquals("anotherValue", composite.getProperty("key"));

        composite.removeProperty("key");
        assertFalse(propsA.containsKey("key"));
        assertTrue(propsB.containsKey("key"));

        composite.setProperty("some", "value");

        assertEquals("value", propsA.getProperty("some"));
    }
}