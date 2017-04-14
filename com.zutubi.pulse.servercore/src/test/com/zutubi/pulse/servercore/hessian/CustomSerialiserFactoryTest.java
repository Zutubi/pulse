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

package com.zutubi.pulse.servercore.hessian;

import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;
import com.caucho.hessian.io.Deserializer;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.mockito.Mockito.*;

public class CustomSerialiserFactoryTest extends PulseTestCase
{
    private CustomSerialiserFactory factory;

    protected void setUp() throws Exception
    {
        super.setUp();

        factory = new CustomSerialiserFactory();
    }

    public void testGetDefaultSerialisers() throws HessianProtocolException
    {
        assertTrue(factory.getSerializer(Enum.class) instanceof EnumSerialiser);
        assertTrue(factory.getSerializer(CustomEnum.class) instanceof EnumSerialiser);
    }

    public void testGetDefaultDeserialisers()
    {
        assertTrue(factory.getDeserializer(Enum.class) instanceof EnumDeserialiser);
        assertTrue(factory.getDeserializer(LogRecord.class) instanceof CustomDeserialiser);
        assertTrue(factory.getDeserializer(Level.class) instanceof CustomDeserialiser);
    }

    public void testCustomRegistration() throws HessianProtocolException
    {
        Serializer s = mock(Serializer.class);
        Deserializer d = mock(Deserializer.class);

        factory.register(Type.class, s, d);

        assertEquals(s, factory.getSerializer(Type.class));
        assertEquals(d, factory.getDeserializer(Type.class));
    }

    private class Type
    {

    }

    private static enum CustomEnum
    {
        A, B, C
    }
}
