package com.zutubi.pulse.servercore.dependency.ivy;

import com.google.common.io.Resources;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptorParser;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.plugins.repository.url.URLResource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;

public class AbstractHessianTestCase extends PulseTestCase
{
    protected File tmpDir;
    protected Ivy ivy;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = createTempDirectory();

        ivy = Ivy.newInstance();
        ivy.configureDefault();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testNothing()
    {
        // to keep idea quiet when running the tests in a directory.
    }

    protected void assertEmptyTmpDir()
    {
        assertEquals(0, tmpDir.list().length);
    }

    protected ModuleDescriptor parseDescriptor(String resourceName) throws IOException, ParseException
    {
        URL url = AbstractHessianTestCase.class.getResource(resourceName);

        URLResource res = new URLResource(url);
        return IvyModuleDescriptorParser.parseDescriptor(ivy.getSettings(), url, res, false);
    }

    protected String readDescriptor(String resourceName) throws IOException
    {
        return Resources.asCharSource(ModuleDescriptorSerialiserTest.class.getResource(resourceName), Charset.defaultCharset()).read();
    }
}
