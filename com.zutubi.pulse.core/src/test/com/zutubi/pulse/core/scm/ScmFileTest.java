package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmFile;
import junit.framework.TestCase;

/**
 *
 *
 */
public class ScmFileTest extends TestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSinglePathComponent()
    {
        ScmFile file = new ScmFile("file");
        assertNull(file.getParent());
        assertEquals("file", file.getPath());
        assertEquals("file", file.getName());

        file = new ScmFile("/file");
        assertNull(file.getParent());
        assertEquals("file", file.getPath());
        assertEquals("file", file.getName());

        file = new ScmFile("file/");
        assertNull(file.getParent());
        assertEquals("file", file.getPath());
        assertEquals("file", file.getName());
    }

    public void testDoublePathComponent()
    {
        ScmFile file = new ScmFile("parent/file");
        assertNotNull(file.getParentFile());
        assertEquals(new ScmFile("parent", true), file.getParentFile());
        assertEquals("parent/file", file.getPath());
        assertEquals("file", file.getName());
        assertEquals("parent", file.getParent());

        file = new ScmFile("/parent/file");
        assertNotNull(file.getParentFile());
        assertEquals(new ScmFile("parent",true), file.getParentFile());
        assertEquals("parent/file", file.getPath());
        assertEquals("file", file.getName());
        assertEquals("parent", file.getParent());

        file = new ScmFile("parent/file/");
        assertNotNull(file.getParentFile());
        assertEquals(new ScmFile("parent", true), file.getParentFile());
        assertEquals("parent/file", file.getPath());
        assertEquals("file", file.getName());
        assertEquals("parent", file.getParent());
    }

    public void testFileEquals()
    {
        assertEquals(new ScmFile("path"), new ScmFile("path"));
        assertEquals(new ScmFile("/path"), new ScmFile("path/"));
        assertEquals(new ScmFile("parent/path"), new ScmFile("parent/path"));
        assertNotSame(new ScmFile("path"), new ScmFile("parent/path"));
        assertNotSame(new ScmFile("path"), new ScmFile("paths"));
        assertNotSame(new ScmFile("parent/path"), new ScmFile("other/parent/path"));
    }
}
