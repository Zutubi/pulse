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

package com.zutubi.pulse.core.ui.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class YesNoResponseTest extends PulseTestCase
{
    public void testFromInputEmpty()
    {
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("", YesNoResponse.ALWAYS, YesNoResponse.values()));
    }

    public void testFromInputWhitespace()
    {
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("  \t", YesNoResponse.ALWAYS, YesNoResponse.values()));
    }

    public void testFromInputKeyChars()
    {
        assertEquals(YesNoResponse.YES, YesNoResponse.fromInput("Y", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.NO, YesNoResponse.fromInput("N", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("A", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.NEVER, YesNoResponse.fromInput("E", null, YesNoResponse.values()));
    }

    public void testFromInputKeyCharLower()
    {
        assertEquals(YesNoResponse.YES, YesNoResponse.fromInput("y", null, YesNoResponse.values()));
    }

    public void testFromInputKeyCharNotInAllowed()
    {
        assertNull(YesNoResponse.fromInput("N", YesNoResponse.YES, YesNoResponse.ALWAYS, YesNoResponse.YES));
    }

    public void testFromInputKeyCharNotRecognised()
    {
        assertNull(YesNoResponse.fromInput("X", YesNoResponse.YES, YesNoResponse.values()));
    }

    public void testFromInputKeyCharWhitespace()
    {
        assertEquals(YesNoResponse.YES, YesNoResponse.fromInput(" Y ", null, YesNoResponse.values()));
    }

    public void testFromInputFull()
    {
        assertEquals(YesNoResponse.YES, YesNoResponse.fromInput("YES", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.NO, YesNoResponse.fromInput("NO", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("ALWAYS", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.NEVER, YesNoResponse.fromInput("NEVER", null, YesNoResponse.values()));
    }

    public void testFromInputFullMixedCase()
    {
        assertEquals(YesNoResponse.NO, YesNoResponse.fromInput("No", null, YesNoResponse.values()));
    }

    public void testFromInputFullWhitespace()
    {
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("  ALWAYS  ", null, YesNoResponse.values()));
    }

    public void testFromInputFullNotInAllowed()
    {
        assertNull(YesNoResponse.fromInput("NO", YesNoResponse.YES, YesNoResponse.YES, YesNoResponse.NEVER));
    }

    public void testFromInputFullNotRecognised()
    {
        assertNull(YesNoResponse.fromInput("YEZ", YesNoResponse.YES, YesNoResponse.values()));
    }

}
