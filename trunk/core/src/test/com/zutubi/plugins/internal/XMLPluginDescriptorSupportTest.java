package com.zutubi.plugins.internal;

import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.PluginsTestCase;
import com.zutubi.plugins.mock.MockComponentDescriptor;
import com.zutubi.plugins.utils.IOUtils;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public class XMLPluginDescriptorSupportTest extends PluginsTestCase
{
    XMLPluginDescriptorSupport support;

    InputStream pluginXml;

    public void setUp() throws Exception
    {
        super.setUp();

        support = new XMLPluginDescriptorSupport();
        support.setDescriptorFactory(descriptorFactory);
    }

    public void tearDown() throws Exception
    {
        IOUtils.close(pluginXml);
        pluginXml = null;

        support = null;

        super.tearDown();
    }

    public void testPluginInfo() throws Exception
    {
        pluginXml = getClass().getResourceAsStream("loaders/sample-plugin.xml");

        Plugin plugin = support.loadPlugin(pluginXml, new DefaultPlugin());
        assertNotNull(plugin);
        assertEquals("Sample plugin", plugin.getName());
        assertEquals("com.zutubi.plugins.sample.SamplePlugin", plugin.getKey());
        assertEquals("This plugin descriptor is just used for test purposes!", plugin.getInfo().getDescription());
        assertEquals("1.0", plugin.getInfo().getPluginVersion());
        assertEquals("1.0", plugin.getInfo().getMinSupportedAppVersion());
        assertEquals("2.1", plugin.getInfo().getMaxSupportedAppVersion());
        assertEquals("Zutubi Pty Ltd", plugin.getInfo().getVendorName());
        assertEquals("http://www.zutubi.com", plugin.getInfo().getVendorUrl());
    }

    public void testComponentDescriptorsAreLoaded() throws Exception
    {
        pluginXml = getClass().getResourceAsStream("loaders/sample-plugin.xml");
        descriptorFactory.addDescriptor("mock", MockComponentDescriptor.class);
        Plugin plugin = support.loadPlugin(pluginXml, new DefaultPlugin());
        assertNotNull(plugin);
        assertEquals(1, plugin.getComponentDescriptors().size());
    }


}
