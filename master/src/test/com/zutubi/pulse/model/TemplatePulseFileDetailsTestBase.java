package com.zutubi.pulse.model;

import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.core.util.IOUtils;
import com.zutubi.pulse.test.PulseTestCase;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 */
public abstract class TemplatePulseFileDetailsTestBase extends PulseTestCase
{
    protected File tmpDir;
    protected VelocityEngine engine;

    protected void setUp() throws Exception
    {
        super.setUp();
        engine = new VelocityEngine();
        File pulseRoot = new File(getPulseRoot(), "master/src/templates");
        engine.setProperty("file.resource.loader.path", pulseRoot.getAbsolutePath());
        engine.init();
        getDetails().setVelocityEngine(engine);
        tmpDir = FileSystemUtils.createTempDirectory(getClass().getName(), "");
    }

    protected void tearDown() throws Exception
    {
        engine = null;
        FileSystemUtils.removeDirectory(tmpDir);
        super.tearDown();
    }

    protected void createAndVerify(String expectedName) throws IOException
    {
        InputStream expectedStream = getInput(expectedName);

        String got = getDetails().getPulseFile(0, null, null);
        String expected = IOUtils.inputStreamToString(expectedStream);

        assertEquals(expected, got);
    }

    public abstract TemplatePulseFileDetails getDetails();
}
