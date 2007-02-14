package com.zutubi.prototype;

import junit.framework.TestCase;

/**
 *
 *
 */
public class PathTest extends TestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testPath()
    {
        Path path = new Path("path/to/somewhere");
        assertEquals("path/to", path.getParent().getPath());
        assertEquals("path", path.getParent().getParent().getPath());

        assertEquals(3, path.getPathElements().size());
    }
}
