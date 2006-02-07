package com.cinnamonbob.bootstrap;

import junit.framework.*;

/**
 * <class-comment/>
 */
public class ConfigurationSupportTest extends TestCase
{
    private ConfigurationSupport support;

    public ConfigurationSupportTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        support = new ConfigurationSupport(new ReadWriteConfiguration());
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        support = null;

        super.tearDown();
    }

    public void testInteger()
    {
        assertNull(support.getInteger("key"));
        assertFalse(support.hasProperty("key"));
        support.setInteger("key", null);
        assertNull(support.getInteger("key"));
        assertFalse(support.hasProperty("key"));
    }
}