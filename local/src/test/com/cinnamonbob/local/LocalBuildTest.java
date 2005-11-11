package com.cinnamonbob.local;

import com.cinnamonbob.test.BobTestCase;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 */
public class LocalBuildTest extends BobTestCase
{
    File tmpDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // Create a temporary working directory
        tmpDir = FileSystemUtils.createTempDirectory(LocalBuildTest.class.getName(), "");
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        FileSystemUtils.removeDirectory(tmpDir);
    }

    private String copyBobFile(String name) throws IOException
    {
        URL bobURL = getInputURL(name);
        File srcFile = new File(bobURL.getFile());
        File destFile = new File(tmpDir, srcFile.getName());

        IOUtils.copyFile(srcFile, destFile);
        return srcFile.getName();
    }

    public void testBasicBuild() throws Exception
    {
        LocalBuild builder = new LocalBuild();
        String bobFile = copyBobFile("basic");

        builder.runBuild(tmpDir, bobFile, "my-default", null, "out");
    }
}
