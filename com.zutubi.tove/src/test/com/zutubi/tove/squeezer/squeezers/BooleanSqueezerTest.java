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

public class BooleanSqueezerTest extends ZutubiTestCase
{
    private BooleanSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();

        squeezer = new BooleanSqueezer();
    }

    public void testNullToString() throws SqueezeException
    {
        assertEquals("", squeezer.squeeze(null));
    }

    public void testBooleanToString() throws SqueezeException
    {
        assertEquals("true", squeezer.squeeze(Boolean.TRUE));
        assertEquals("false", squeezer.squeeze(Boolean.FALSE));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("false", squeezer.squeeze(false));
        assertEquals("true", squeezer.squeeze(true));
    }

    public void testStringToBoolean() throws SqueezeException
    {
        assertEquals(Boolean.FALSE, squeezer.unsqueeze("false"));
        assertEquals(Boolean.FALSE, squeezer.unsqueeze("0"));
        assertEquals(Boolean.TRUE, squeezer.unsqueeze("true"));
    }

}
