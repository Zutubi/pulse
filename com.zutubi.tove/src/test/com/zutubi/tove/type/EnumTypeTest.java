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

package com.zutubi.tove.type;

/**
 */
public class EnumTypeTest extends TypeTestCase
{
    private EnumType type;


    protected void setUp() throws Exception
    {
        type = new EnumType(TestEnum.class);
    }

    public void testToXmlRpcNull() throws TypeException
    {
        assertNull(type.toXmlRpc(null, null));
    }
    
    public void testToXmlRpc() throws TypeException
    {
        Object o = type.toXmlRpc(null, "M2");
        assertTrue(o instanceof String);
        assertEquals("M2", o);
    }

    public void testFromXmlRpc() throws TypeException
    {
        Object o = type.fromXmlRpc(null, "M2", true);
        assertTrue(o instanceof String);
        assertEquals("M2", o);
    }

    public void testFromXmlRpcWrongType()
    {
        try
        {
            type.fromXmlRpc(null, new Integer(2), true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("Expecting 'java.lang.String', found 'java.lang.Integer'", e.getMessage());
        }
    }

    private enum TestEnum
    {
        M1,
        M2,
        M3,
    }
}
