package com.cinnamonbob.local;

import junit.framework.*;

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
        File work = getAbsolute("/c/tmp");
        File output = getAbsolute("/d/tmp");

        LocalRecipePaths paths = new LocalRecipePaths(work, output.getAbsolutePath());

        assertEquals(work, paths.getWorkDir());
        assertEquals(output, paths.getOutputDir());
    }

    public void testRelativeOutputPath()
    {
        File work = getAbsolute("/c/tmp");
        File output = getAbsolute("/c/tmp/relative");

        LocalRecipePaths paths = new LocalRecipePaths(work, "relative");

        assertEquals(work, paths.getWorkDir());
        assertEquals(output, paths.getOutputDir());
    }

    public void testRelativeWork()
    {
        File work = new File("tmp");
        File output = new File("tmp", "relative");

        LocalRecipePaths paths = new LocalRecipePaths(work, "relative");

        assertEquals(work, paths.getWorkDir());
        assertEquals(output, paths.getOutputDir());
    }

    private File getAbsolute(String str)
    {
        return new File(str).getAbsoluteFile();
    }
}