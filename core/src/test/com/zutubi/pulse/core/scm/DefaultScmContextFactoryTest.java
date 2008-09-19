package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;

public class DefaultScmContextFactoryTest extends PulseTestCase
{
    private DefaultScmContextFactory factory;
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        // i would like to mock this directory out, but can not see a way to do so with mockito - considering
        // that the context factory attempts to create a new file based on this file.  Maybe insert a
        // file system interface?
        tmp = FileSystemUtils.createTempDir();

        factory = new DefaultScmContextFactory();
        factory.setProjectsDir(tmp);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        tmp = null;
        factory = null;

        super.tearDown();
    }

    public void testScmContextCorrectlyConfigured() throws ScmException
    {
        ScmContext context = factory.createContext(1, null);
        File expectedDir = new File(tmp, FileSystemUtils.join("1", "scm"));
        assertEquals(expectedDir, context.getPersistentWorkingDir());
    }
}
