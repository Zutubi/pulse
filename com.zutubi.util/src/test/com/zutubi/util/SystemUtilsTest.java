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

package com.zutubi.util;

import com.google.common.base.Predicate;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static com.google.common.collect.Iterables.any;
import static java.util.Arrays.asList;

/**
 * NOTE: cygwin is requried on the path for windows machines.
 */
public class SystemUtilsTest extends ZutubiTestCase
{
    public void testFindInPathNonExistant()
    {
        assertNull(SystemUtils.findInPath("nonexistantfile"));
    }

    public void testFindInPathList()
    {
        String list;

        if (SystemUtils.IS_WINDOWS)
        {
            list = "cmd";
        }
        else
        {
            list = "ls";
        }

        File bin = SystemUtils.findInPath(list);
        assertNotNull(bin);
    }

    public void testFindInPathComFile()
    {
        if(SystemUtils.IS_WINDOWS)
        {
            assertNotNull(SystemUtils.findInPath("more"));
        }
    }

    public void testFindInPathCaseInsensitive()
    {
        if(SystemUtils.IS_WINDOWS)
        {
            assertNotNull(SystemUtils.findInPath("CmD"));
        }
    }

    public void testFindInPathFindsMaven()
    {
        File mvn = SystemUtils.findInPath("mvn");
        if(mvn != null)
        {
            if(SystemUtils.IS_WINDOWS)
            {
                assertEquals("mvn.bat", mvn.getName());
            }
            else
            {
                assertEquals("mvn", mvn.getName());
            }
        }
    }

    public void testThreadDump()
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SystemUtils.threadDump(new PrintStream(baos));

        String[] dump = new String(baos.toByteArray()).split("\n");
        assertTrue(any(asList(dump), new Predicate<String>()
        {
            public boolean apply(String s)
            {
                return s.contains("at com.zutubi.util.SystemUtils.threadDump");
            }
        }));
    }

/*
    public void testFindInPathExtraPaths() throws IOException, URISyntaxException
    {
        // TODO: getTestDataFile is part of PulseTestCase, not currently available at this level.  Should it be?
        File extraPath = getTestDataFile("core", "findInPathExtraPaths", "data");
        File bin = SystemUtils.findInPath("dir", Arrays.asList(new String [] { extraPath.getAbsolutePath() } ));
        assertNotNull(bin);
        assertEquals(extraPath.getCanonicalPath(), bin.getParentFile().getCanonicalPath());
    }
*/
}
