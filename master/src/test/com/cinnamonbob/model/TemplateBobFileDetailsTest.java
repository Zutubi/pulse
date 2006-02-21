package com.cinnamonbob.model;

import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.test.BobTestCase;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 */
public abstract class TemplateBobFileDetailsTest extends BobTestCase
{
    protected File tmpDir;
    protected VelocityEngine engine;

    protected void setUp() throws Exception
    {
        super.setUp();
        engine = new VelocityEngine();
        File bobRoot = new File(getBobRoot(), "master/src/templates");
        engine.setProperty("file.resource.loader.path", bobRoot.getAbsolutePath());
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

        String got = getDetails().getBobFile(null, null);
        String expected = IOUtils.inputStreamToString(expectedStream);

        assertEquals(expected, got);
    }

    public abstract TemplateBobFileDetails getDetails();
}
