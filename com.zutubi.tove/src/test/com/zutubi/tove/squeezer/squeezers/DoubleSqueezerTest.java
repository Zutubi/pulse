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
public class DoubleSqueezerTest extends ZutubiTestCase
{
    private DoubleSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();
        squeezer = new DoubleSqueezer();
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

    public void testDoubleToString() throws SqueezeException
    {
        assertEquals("1.0", squeezer.squeeze(new Double(1.0d)));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("0.015", squeezer.squeeze(0.015d));
    }

    public void testStringToDouble() throws SqueezeException
    {
        assertEquals(1.5d, squeezer.unsqueeze("1.5"));
    }

    public void testEmptyStringToDouble() throws SqueezeException
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
            assertEquals("'a' is not a valid double", e.getMessage());
        }
    }
}
