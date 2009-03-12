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
