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

package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.List;

public class ConfigurationCleanupTaskFinderTest extends ZutubiTestCase
{
    private ObjectFactory objectFactory;

    protected void setUp() throws Exception
    {
        objectFactory = new DefaultObjectFactory();
    }

    public void testNoMatchingCleanupTasksMethod() throws Exception
    {
        assertTasks(NoMatchingCleanupTasksMethod.class);
    }

    public void testNoArgCleanupTasksMethod() throws Exception
    {
        assertTasks(NoArgCleanupTasksMethod.class, "no arg");
    }

    public void testArgCleanupTasksMethod() throws Exception
    {
        assertTasks(ArgCleanupTasksMethod.class, getName());
    }

    private void assertTasks(Class cleanupTasksClass, String... paths) throws Exception
    {
        List<RecordCleanupTask> tasks = getTasks(cleanupTasksClass);
        assertEquals(paths.length, tasks.size());
        for(int i = 0; i < paths.length; i++)
        {
            assertEquals(paths[i], tasks.get(i).getAffectedPath());
        }
    }

    private List<RecordCleanupTask> getTasks(Class cleanupTasksClass) throws Exception
    {
        ConfigurationCleanupTaskFinder finder = new ConfigurationCleanupTaskFinder(TrivialConfiguration.class, cleanupTasksClass, objectFactory);
        return finder.getCleanupTasks(new TrivialConfiguration(getName()));
    }

    public static class TrivialConfiguration extends AbstractConfiguration
    {
        public TrivialConfiguration(String path)
        {
            setConfigurationPath(path);
        }
    }

    public static class NoMatchingCleanupTasksMethod
    {
        public List<String> getTasks()
        {
            return null;
        }

        public List<RecordCleanupTask> getTasks(String s)
        {
            return null;
        }

        public List<RecordCleanupTask> getTasks(TrivialConfiguration config, String s)
        {
            return null;
        }
    }

    public static class NoArgCleanupTasksMethod
    {
        public List<RecordCleanupTask> getTasks()
        {
            return Arrays.<RecordCleanupTask>asList(new NoopCleanupTask("no arg"));
        }
    }

    public static class ArgCleanupTasksMethod
    {
        public List<RecordCleanupTask> getTasks(TrivialConfiguration c)
        {
            return Arrays.<RecordCleanupTask>asList(new NoopCleanupTask(c.getConfigurationPath()));
        }
    }

    public static class NoopCleanupTask extends RecordCleanupTaskSupport
    {
        public NoopCleanupTask(String path)
        {
            super(path);
        }

        public boolean run(RecordManager recordManager)
        {
            // Do nothing
            return true;
        }

        public CleanupAction getCleanupAction()
        {
            return CleanupAction.NONE;
        }
    }
}
