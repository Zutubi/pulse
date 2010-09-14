package com.zutubi.tove.config;

import com.zutubi.tove.config.events.ConfigurationEvent;
import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;

/**
 * Simple configuration event listener that records the events it receives.
 */
public class RecordingConfigurationEventListener implements ConfigurationEventListener
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
