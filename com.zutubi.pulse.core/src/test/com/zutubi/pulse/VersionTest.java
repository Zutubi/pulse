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

package com.zutubi.pulse;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 */
public class VersionTest extends PulseTestCase
{
    public void testDefaultVersion()
    {
        Version v = Version.getVersion();
        assertEquals("@BUILD_DATE@", v.getBuildDate());
        assertEquals("@BUILD_NUMBER@", v.getBuildNumber());
        assertEquals("@VERSION@", v.getVersionNumber());
        assertEquals("@RELEASE_DATE@", v.getReleaseDate());
    }

    public void testReadWriteVersion() throws IOException
    {
        Version v = new Version("a", "b", "c", "d");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        v.write(out);

        Version v2 = Version.load(new ByteArrayInputStream(out.toByteArray()));

        assertEquals(v.getVersionNumber(), v2.getVersionNumber());
        assertEquals(v.getBuildNumber(), v2.getBuildNumber());
        assertEquals(v.getBuildDate(), v2.getBuildDate());
        assertEquals(v.getReleaseDate(), v2.getReleaseDate());
    }

    public void testbuildNumberToVersion()
    {
        assertEquals("1.2.11", Version.buildNumberToVersion(102011000));
    }

    public void testbuildNumberToVersionPatch()
    {
        assertEquals("1.2.101", Version.buildNumberToVersion(102101333));
    }

    public void testbuildNumberToVersionZeroBuild()
    {
        assertEquals("1.2.0", Version.buildNumberToVersion(102000000));
    }
}