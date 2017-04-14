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

package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.util.junit.ZutubiTestCase;

/**
 */
public class CharacterSqueezerTest extends ZutubiTestCase
{
    private CharacterSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();
        squeezer = new CharacterSqueezer();
    }

    protected void tearDown() throws Exception
    {
        squeezer = null;
        super.tearDown();
    }

    public void testNullToString() throws SqueezeException
    {
        assertEquals("", squeezer.squeeze(null));
    }

    public void testCharacterToString() throws SqueezeException
    {
        assertEquals("g", squeezer.squeeze(new Character('g')));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("a", squeezer.squeeze('a'));
    }

    public void testStringToCharacter() throws SqueezeException
    {
        assertEquals('t', squeezer.unsqueeze("t"));
    }

    public void testEmptyStringToCharacter() throws SqueezeException
    {
        assertNull(squeezer.unsqueeze(""));
    }
}
