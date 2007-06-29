package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.ConfigurationEvent;
import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class MockConfigurationEventListener implements ConfigurationEventListener
{
    private List<ConfigurationEvent> actualEvents = new LinkedList<ConfigurationEvent>();
    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        actualEvents.add(event);
    }

    public void assertNextEvent(Class expectedType, String expectedPath)
    {
        Assert.assertTrue(actualEvents.size() > 0);
        ConfigurationEvent next = actualEvents.remove(0);
        Assert.assertEquals(expectedType, next.getClass());
        Assert.assertEquals(expectedPath, next.getInstance().getConfigurationPath());
    }

    public void assertNoMoreEvents()
    {
        Assert.assertEquals(0, actualEvents.size());
    }
}
