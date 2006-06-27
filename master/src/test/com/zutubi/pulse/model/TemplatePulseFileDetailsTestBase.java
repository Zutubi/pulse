package com.zutubi.pulse.model;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.model.Property;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.LinkedList;

/**
 */
public abstract class TemplatePulseFileDetailsTestBase extends PulseTestCase
{
    protected File tmpDir;
    protected VelocityEngine engine;
    protected boolean generateMode = false;

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

    protected void createAndVerify(String expectedName) throws Exception
    {
        String got = getDetails().getPulseFile(0, null, null);
        File file = new File(getPulseRoot(), FileSystemUtils.composeFilename("master", "src", "test", "com", "zutubi", "pulse", "model", getClass().getSimpleName() + "." + expectedName + ".xml"));

        if(generateMode)
        {
            FileSystemUtils.createFile(file, got);
        }
        else
        {
            InputStream expectedStream = null;

            try
            {
                expectedStream = new FileInputStream(file);
                String expected = IOUtils.inputStreamToString(expectedStream);
                assertEquals(expected, got);
                expectedStream.close();
            }
            finally
            {
                IOUtils.close(expectedStream);
            }
        }

        // Ensure syntactic correctness
        PulseFileLoader loader = new PulseFileLoader(new ObjectFactory(), new FileResourceRepository());
        List<Reference> references = new LinkedList<Reference>();
        references.add(new Property("base.dir", "testbase"));
        FileInputStream input = null;

        try
        {
            input = new FileInputStream(file);
            loader.load(input, new PulseFile(), references, null);
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    public abstract TemplatePulseFileDetails getDetails();

    protected void addCaptures(TemplatePulseFileDetails details)
    {
        FileCapture capture = new FileCapture("artifact 1", "filename");
        capture.addProcessor("junit");
        details.addCapture(capture);

        capture = new FileCapture("artifact 2", "filename");
        capture.addProcessor("junit");
        details.addCapture(capture);
    }
}
