package com.zutubi.pulse.core.test;

import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Base class for test cases.
 */
public abstract class PulseTestCase extends ZutubiTestCase
{
    public PulseTestCase()
    {
    }

    public PulseTestCase(String name)
    {
        super(name);
    }

    protected InputStream getInput(String testName)
    {
        return getInput(testName, "xml");
    }

    protected InputStream getInput(String testName, String extension)
    {
        return getClass().getResourceAsStream(getClass().getSimpleName() + "." + testName + "." + extension);
    }

    protected URL getInputURL(String testName)
    {
        return getInputURL(testName, "xml");
    }

    protected URL getInputURL(String testName, String extension)
    {
        return getClass().getResource(getClass().getSimpleName() + "." + testName + "." + extension);
    }

    protected File getTestDataDir(String module, String name)
    {
        return new File(getPulseRoot(), FileSystemUtils.composeFilename(module, "src", "test", getClass().getPackage().getName().replace('.', File.separatorChar), name));
    }

    protected File getTestDataFile(String module, String testName, String extension)
    {
        String testPart = testName == null ? "" : "." + testName;
        return new File(getPulseRoot(), FileSystemUtils.composeFilename(module, "src", "test", getClass().getName().replace('.', File.separatorChar) + testPart + "." + extension));
    }

    public static File getPulseRoot()
    {
        return TestUtils.getPulseRoot();
    }

    public static void removeDirectory(File dir) throws IOException
    {
        if (!FileSystemUtils.rmdir(dir))
        {
            throw new IOException("Failed to remove " + dir);
        }
    }
}
