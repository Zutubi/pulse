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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;

public class MavenUtilsTest extends PulseTestCase
{
    private static final String EXTENSION_XML = "xml";
    private static final String ELEMENT_VERSION = "version";

    public void testExpectedFile() throws PulseException
    {
        assertEquals("1.0-SNAPSHOT", MavenUtils.extractVersion(getInputFile(EXTENSION_XML), ELEMENT_VERSION));
    }

    public void testNoVersionInFile() throws PulseException
    {
        assertNull(MavenUtils.extractVersion(getInputFile(EXTENSION_XML), ELEMENT_VERSION));
    }

    public void testNonParseableFile() throws PulseException
    {
        try
        {
            MavenUtils.extractVersion(getInputFile(EXTENSION_XML), ELEMENT_VERSION);
            fail("File should not be parseable");
        }
        catch (PulseException e)
        {
            assertThat(e.getMessage(), containsString("Unable to parse"));
        }
    }

    public void testFileDoesNotExist() throws PulseException
    {
        assertNull(MavenUtils.extractVersion(new File("there is no such file"), ELEMENT_VERSION));
    }
}
