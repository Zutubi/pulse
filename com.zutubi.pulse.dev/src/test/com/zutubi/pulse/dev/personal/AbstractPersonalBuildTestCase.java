package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Pair;
import com.zutubi.util.config.Config;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Base for classes that test personal build functionality.  Sets up an
 * environment for personal build configuration.
 */
public abstract class AbstractPersonalBuildTestCase extends PulseTestCase
{
    private File tempDir;
    protected File baseParentDir;
    protected File baseDir;
    protected File userHomeDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = createTempDirectory();
        baseParentDir = new File(tempDir, "intermediate");
        baseDir = new File(baseParentDir, "base");
        assertTrue(baseDir.mkdirs());
        userHomeDir = new File(tempDir, "home");
        assertTrue(userHomeDir.mkdir());
        System.setProperty("user.home", userHomeDir.getAbsolutePath());
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    protected void createProperties(File dir, Pair<String, String>... properties) throws IOException
    {
        Properties p = new Properties();
        for (Pair<String, String> property: properties)
        {
            p.put(property.first, property.second);
        }

        IOUtils.write(p, new File(dir, PersonalBuildConfig.PROPERTIES_FILENAME));
    }

    protected void assertProperties(File dir, Pair<String, String>... expectedProperties) throws IOException
    {
        File propertiesFile = new File(dir, PersonalBuildConfig.PROPERTIES_FILENAME);
        Properties properties;
        if (propertiesFile.exists())
        {
            properties = IOUtils.read(propertiesFile);
        }
        else
        {
            properties = new Properties();
        }

        assertProperties(properties, expectedProperties);
    }

    protected void assertProperties(Config config, Pair<String, String>... expectedProperties)
    {
        for (Pair<String, String> expected: expectedProperties)
        {
            assertEquals(expected.second, config.getProperty(expected.first));
        }
    }

    protected void assertProperties(Properties properties, Pair<String, String>... expectedProperties)
    {
        assertEquals(expectedProperties.length, properties.size());
        for (Pair<String, String> expected: expectedProperties)
        {
            assertEquals(expected.second, properties.get(expected.first));
        }
    }
}
