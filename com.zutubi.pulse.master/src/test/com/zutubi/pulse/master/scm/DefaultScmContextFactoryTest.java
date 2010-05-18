package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.api.PulseTestCase;

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
        tmp = createTempDirectory();

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
/*
        ScmContext context = factory.createContext(1, null);
        File expectedDir = new File(tmp, FileSystemUtils.join("1", "scm"));
        assertEquals(expectedDir, context.getPersistentWorkDir());
*/
    }
}
