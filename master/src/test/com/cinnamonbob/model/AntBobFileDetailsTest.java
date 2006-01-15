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
public class AntBobFileDetailsTest extends BobTestCase
{
    private File tmpDir;
    private AntBobFileDetails details;

    protected void setUp() throws Exception
    {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty("file.resource.loader.path", "/home/jsankey/svn/bob/trunk/master/src/templates");
        engine.init();
        details = new AntBobFileDetails();
        details.setVelocityEngine(engine);

        tmpDir = FileSystemUtils.createTempDirectory(AntBobFileDetailsTest.class.getName(), "");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        FileSystemUtils.removeDirectory(tmpDir);
    }

    public void testBasic() throws IOException
    {
        createAndVerify("basic");
    }

    public void testExplicitBuildFile() throws IOException
    {
        details.setBuildFile("test.xml");
        createAndVerify("explicitBuildFile");
    }

    public void testEnvironment() throws IOException
    {
        details.addEnvironmentalVariable("var", "value");
        details.addEnvironmentalVariable("var2", "value2");
        createAndVerify("environment");
    }

    private void createAndVerify(String expectedName) throws IOException
    {
        InputStream expectedStream = getInput(expectedName);

        String got = details.getBobFile(null, null);
        String expected = IOUtils.inputStreamToString(expectedStream);

        assertEquals(expected, got);
    }
}
