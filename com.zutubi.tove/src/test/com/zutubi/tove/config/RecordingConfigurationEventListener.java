/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
