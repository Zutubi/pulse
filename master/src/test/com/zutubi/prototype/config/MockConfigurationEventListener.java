package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.ConfigurationEvent;

import java.util.List;
import java.util.LinkedList;

import junit.framework.Assert;

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

    public void clear()
    {
        actualEvents.clear();
    }

    public void expected(Class... expectedEvents)
    {
        Assert.assertEquals(expectedEvents.length, actualEvents.size());
        for (int i = 0; i < expectedEvents.length; i++)
        {
            Class expected = expectedEvents[i];
            ConfigurationEvent actual = actualEvents.get(i);
            Assert.assertTrue(expected == actual.getClass());
        }
    }
}
