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

package com.zutubi.pulse.core.util.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class XMLUtilsTest extends PulseTestCase
{
    public void testRemoveIllegalCharactersEmpty()
    {
        assertEquals("", XMLUtils.removeIllegalCharacters(""));
    }

    public void testRemoveIllegalCharactersOneLegal()
    {
        assertEquals("a", XMLUtils.removeIllegalCharacters("a"));
    }

    public void testRemoveIllegalCharactersManyLegal()
    {
        String s = "many\rlegal\ncharacters\there and there and <>?,./;'][|}{+_~!@#$%^&*()_=-0987654321`\\\" everywhere";
        assertEquals(s, XMLUtils.removeIllegalCharacters(s));
    }
    
    public void testRemoveIllegalCharactersOneIllegal()
    {
        assertEquals("", XMLUtils.removeIllegalCharacters("\u0000"));
    }

    public void testRemoveIllegalCharactersManyIllegal()
    {
        assertEquals("", XMLUtils.removeIllegalCharacters("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u000B\u000C\u000E\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F"));
    }

    public void testRemoveIllegalCharactersLegalIllegal()
    {
        assertEquals("a", XMLUtils.removeIllegalCharacters("a\u0000"));
    }
    
    public void testRemoveIllegalCharactersIllegalLegal()
    {
        assertEquals("a", XMLUtils.removeIllegalCharacters("\u0000a"));
    }

    public void testRemoveIllegalCharactersMixed()
    {
        assertEquals("abcdefghi", XMLUtils.removeIllegalCharacters("\u0000ab\u0011c\u0012\u0002def\u0000g\u0001\u0001\u0001hi"));
    }

    public void testEscapeEmpty()
    {
        assertEquals("", XMLUtils.escape(""));
    }

    public void testEscapeWhitespace()
    {
        assertEquals(" \n \t", XMLUtils.escape(" \n \t"));
    }

    public void testEscapeSpecials()
    {
        assertEquals("&lt;this&gt; is bad &amp; so is &lt;that&gt;", XMLUtils.escape("<this> is bad & so is <that>"));
    }
}
