package com.cinnamonbob.local;

import junit.framework.TestCase;

import java.io.File;

/**
 * <class-comment/>
 */
public class LocalRecipePathsTest extends TestCase
{

    public LocalRecipePathsTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

        super.tearDown();
    }

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