package com.zutubi.util.config;

import junit.framework.Assert;
import com.zutubi.util.junit.ZutubiTestCase;

public class CompositeConfigTest extends ZutubiTestCase
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
        Assert.assertFalse(config.hasProperty("some.property"));
        Assert.assertNull(config.getProperty("some.property"));
        Assert.assertFalse(config.isWritable());

        // unable to store content.
        config.setProperty("some.property", "value");
        Assert.assertNull(config.getProperty("some.property"));
    }

    public void testOneDelegate()
    {
        PropertiesConfig delegate = new PropertiesConfig();

        // check that all methods behave as expected.
        CompositeConfig config = new CompositeConfig(delegate);
        Assert.assertEquals(delegate.hasProperty("some.property"), config.hasProperty("some.property"));
        Assert.assertEquals(delegate.getProperty("some.property"), config.getProperty("some.property"));
        Assert.assertEquals(delegate.isWritable(), config.isWritable());

        // unable to store content.
        config.setProperty("some.property", "value");
        Assert.assertEquals(delegate.getProperty("some.property"), config.getProperty("some.property"));
    }
}
