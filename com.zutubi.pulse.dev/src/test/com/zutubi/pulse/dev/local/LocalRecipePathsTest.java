package com.zutubi.pulse.dev.local;

import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;

public class LocalRecipePathsTest extends ZutubiTestCase
{
    public void testAbsoluteOutputPath()
    {
        File base = getAbsolute("/c/tmp");
        File output = getAbsolute("/d/tmp");

        LocalRecipePaths paths = new LocalRecipePaths(base, output.getAbsolutePath());

        assertEquals(base, paths.getBaseDir());
        assertEquals(output, paths.getOutputDir());
    }

    public void testRelativeOutputPath()
    {
        File base = getAbsolute("/c/tmp");
        File output = getAbsolute("/c/tmp/relative");

        LocalRecipePaths paths = new LocalRecipePaths(base, "relative");

        assertEquals(base, paths.getBaseDir());
        assertEquals(output, paths.getOutputDir());
    }

    public void testRelativeWork()
    {
        File base = new File("tmp");
        File output = new File("tmp", "relative");

        LocalRecipePaths paths = new LocalRecipePaths(base, "relative");

        assertEquals(base, paths.getBaseDir());
        assertEquals(output, paths.getOutputDir());
    }

    private File getAbsolute(String str)
    {
        return new File(str).getAbsoluteFile();
    }
}