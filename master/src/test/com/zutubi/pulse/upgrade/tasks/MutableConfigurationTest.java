package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.test.PulseTestCase;

import java.util.Arrays;
import java.io.IOException;

/**
 *
 *
 */
public class MutableConfigurationTest extends PulseTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testPropertiesCopied()
    {
        MutableConfiguration config = new MutableConfiguration();
        config.setProperty("a", "b");

        MutableConfiguration copy = config.copy();
        assertEquals(config.getProperty("a"), copy.getProperty("a"));
    }

    public void testMappingsCopied() throws IOException
    {
        MutableConfiguration config = new MutableConfiguration();
        assertNull(config.getClassMapping("types"));
        
        config.addClassPathMappings(Arrays.asList("com/zutubi/pulse/transfer/Schema.hbm.xml"));
        assertNotNull(config.getClassMapping("types"));

        MutableConfiguration copy = config.copy();
        assertNotNull(copy.getClassMapping("types"));
    }
}
