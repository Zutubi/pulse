package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.config.MockScmConfiguration;
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
        MockScmConfiguration config = new MockScmConfiguration();
        config.setHandle(1);
        ScmContext context = factory.createContext(1, config);
        File expectedDir = new File(tmp, FileSystemUtils.join("1", "scm"));
        assertEquals(expectedDir, context.getPersistentWorkingDir());
    }

    public void testScmContextReused() throws ScmException
    {
        MockScmConfiguration config = new MockScmConfiguration();
        config.setHandle(1);
        ScmContext a = factory.createContext(1, config);
        ScmContext b = factory.createContext(1, config);
        assertSame(a, b);
    }

}
