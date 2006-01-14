package com.cinnamonbob.model;

import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.test.BobTestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 */
public class AntBobFileSourceTest extends BobTestCase
{
    private File tmpDir;

    protected void setUp() throws Exception
    {
        tmpDir = FileSystemUtils.createTempDirectory(AntBobFileSourceTest.class.getName(), "");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        FileSystemUtils.removeDirectory(tmpDir);
    }

    public void testBasic() throws IOException
    {
        AntBobFileDetails source = new AntBobFileDetails();
        createAndVerify(source, "basic");
    }

    public void testExplicitBuildFile() throws IOException
    {
        AntBobFileDetails source = new AntBobFileDetails();
        source.setBuildFile("test.xml");
        createAndVerify(source, "explicitBuildFile");
    }

    public void testEnvironment() throws IOException
    {
        AntBobFileDetails source = new AntBobFileDetails();
        source.addEnvironmentalVariable("var", "value");
        source.addEnvironmentalVariable("var2", "value2");
        createAndVerify(source, "environment");
    }

    private void createAndVerify(AntBobFileDetails source, String expectedName) throws IOException
    {
        InputStream expectedStream = getInput(expectedName);

        String got = source.getBobFile(null, null);
        String expected = IOUtils.inputStreamToString(expectedStream);

        assertEquals(expected, got);
    }
}
