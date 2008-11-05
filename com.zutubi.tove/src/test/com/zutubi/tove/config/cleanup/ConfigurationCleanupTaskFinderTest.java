package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.config.AbstractConfiguration;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.List;

/**
 */
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
        ConfigurationCleanupTaskFinder finder = new ConfigurationCleanupTaskFinder(MockConfiguration.class, cleanupTasksClass, objectFactory);
        return finder.getCleanupTasks(new MockConfiguration(getName()));
    }

    public static class MockConfiguration extends AbstractConfiguration
    {
        public MockConfiguration(String path)
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

        public List<RecordCleanupTask> getTasks(MockConfiguration config, String s)
        {
            return null;
        }
    }

    public static class NoArgCleanupTasksMethod
    {
        public List<RecordCleanupTask> getTasks()
        {
            return Arrays.<RecordCleanupTask>asList(new MockCleanupTask("no arg"));
        }
    }

    public static class ArgCleanupTasksMethod
    {
        public List<RecordCleanupTask> getTasks(MockConfiguration c)
        {
            return Arrays.<RecordCleanupTask>asList(new MockCleanupTask(c.getConfigurationPath()));
        }
    }

    public static class MockCleanupTask extends RecordCleanupTaskSupport
    {
        public MockCleanupTask(String path)
        {
            super(path);
        }

        public void run()
        {
            // Do nothing
        }
    }
}
