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
public class ByteSqueezerTest extends ZutubiTestCase
{
    private ByteSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();
        squeezer = new ByteSqueezer();
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

    public void testByteToString() throws SqueezeException
    {
        assertEquals("25", squeezer.squeeze(new Byte((byte) 25)));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("15", squeezer.squeeze((byte)15));
    }

    public void testStringToByte() throws SqueezeException
    {
        assertEquals((byte)15, squeezer.unsqueeze("15"));
    }

    public void testEmptyStringToByte() throws SqueezeException
    {
        assertNull(squeezer.unsqueeze(""));
    }

    public void testInvalid() throws SqueezeException
    {
        try
        {
            squeezer.unsqueeze("a");
            fail();
        }
        catch (SqueezeException e)
        {
            assertEquals("'a' is not a valid byte value", e.getMessage());
        }
    }
}
