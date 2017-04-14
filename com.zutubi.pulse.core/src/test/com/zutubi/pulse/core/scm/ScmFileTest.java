/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmFile;
import com.zutubi.util.junit.ZutubiTestCase;

/**
 *
 *
 */
public class ScmFileTest extends ZutubiTestCase
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
