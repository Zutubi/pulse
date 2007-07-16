package com.zutubi.pulse.plugins;

import junit.framework.TestCase;

/**
 *
 *
 */
public class PluginRegistryTest extends TestCase
{
    private PluginRegistry registry;

    protected void setUp() throws Exception
    {
        super.setUp();

        registry = new PluginRegistry();
    }

    protected void tearDown() throws Exception
    {
        registry = null;

        super.tearDown();
    }
}
