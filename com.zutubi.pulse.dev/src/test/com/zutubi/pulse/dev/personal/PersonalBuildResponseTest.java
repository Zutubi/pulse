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

package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import nu.xom.ParsingException;

import java.io.IOException;
import java.util.Arrays;

public class PersonalBuildResponseTest extends PulseTestCase
{
    private static final String EXTENSION_XML = "xml";

    public void testSuccess() throws IOException, ParsingException
    {
        PersonalBuildResponse response = PersonalBuildResponse.parse(getInput(EXTENSION_XML));
        assertTrue(response.isSuccess());
        assertEquals(2, response.getNumber());
    }

    public void testWarnings() throws IOException, ParsingException
    {
        PersonalBuildResponse response = PersonalBuildResponse.parse(getInput(EXTENSION_XML));
        assertTrue(response.isSuccess());
        assertEquals(57, response.getNumber());
        assertEquals(Arrays.asList("warning 1", "warning 2"), response.getWarnings());
    }

    public void testErrors() throws IOException, ParsingException
    {
        PersonalBuildResponse response = PersonalBuildResponse.parse(getInput(EXTENSION_XML));
        assertFalse(response.isSuccess());
        assertEquals(Arrays.asList("error 1", "error 2"), response.getErrors());
    }
}
