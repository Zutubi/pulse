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

package com.zutubi.pulse.core.upgrade;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystemUtils;
import nu.xom.ParsingException;

import java.io.IOException;

import static com.zutubi.util.io.FileSystemUtils.normaliseNewlines;

public class PulseFileToToveFileTest extends PulseTestCase
{
    private static final String EXTENSION_XML = "xml";

    public void testRemoveResource() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testRemoveResourceFromCommand() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testPullUpArtifacts() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testPushDownCommandName() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testRecipeVersion() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testOrderingPreserved() throws IOException, ParsingException
    {
        expectedOutputHelper();
    }

    public void testWhitespacePreserved() throws IOException, ParsingException
    {
        expectedOutputHelper(false);
    }

    public void testUTF8BOM() throws IOException, ParsingException
    {
        // This is just to check the parser itself does not throw an exception.
        PulseFileToToveFile.convert(getInput(EXTENSION_XML));
    }

    private void expectedOutputHelper() throws IOException, ParsingException
    {
        expectedOutputHelper(true);
    }

    private void expectedOutputHelper(boolean stripWhitespace) throws IOException, ParsingException
    {
        String in = readInputFully(getName() + ".in", EXTENSION_XML);
        String out = PulseFileToToveFile.convert(in);
        String expected = readInputFully(getName() + ".out", EXTENSION_XML);
        if (stripWhitespace)
        {
            out = stripWhitespace(out);
            expected = stripWhitespace(expected);
        }
        else
        {
            out = normaliseNewlines(out);
            expected = normaliseNewlines(expected);
        }
        assertEquals(expected, out);
    }

    private String stripWhitespace(String out)
    {
        return out.replaceAll("\\s+", "");
    }

}
