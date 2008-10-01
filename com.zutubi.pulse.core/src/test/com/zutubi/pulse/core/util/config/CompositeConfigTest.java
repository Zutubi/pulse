package com.zutubi.pulse.core.util.config;

import com.zutubi.pulse.core.test.PulseTestCase;

/**
 * <class-comment/>
 */
public class CompositeConfigTest extends PulseTestCase
{
    public CompositeConfigTest()
    {
    }

    public CompositeConfigTest(String name)
    {
        super(name);
    }

    public void testNoDelegates()
    {
        // check that all methods behave as expected.
        CompositeConfig config = new CompositeConfig();
        assertFalse(config.hasProperty("some.property"));
        assertNull(config.getProperty("some.property"));
        assertFalse(config.isWriteable());

        // unable to store content.
        config.setProperty("some.property", "value");
        assertNull(config.getProperty("some.property"));
    }

    public void testOneDelegate()
    {
        PropertiesConfig delegate = new PropertiesConfig();

        // check that all methods behave as expected.
        CompositeConfig config = new CompositeConfig(delegate);
        assertEquals(delegate.hasProperty("some.property"), config.hasProperty("some.property"));
        assertEquals(delegate.getProperty("some.property"), config.getProperty("some.property"));
        assertEquals(delegate.isWriteable(), config.isWriteable());

        // unable to store content.
        config.setProperty("some.property", "value");
        assertEquals(delegate.getProperty("some.property"), config.getProperty("some.property"));
    }
}
