package com.zutubi.pulse.hessian;

import com.caucho.hessian.io.*;

/**
 * A serialiser factory that handles Java 5 enums.
 *
 * Adapted from an implementation posted to hessian-interest by Jason Stiefel.
 */
public class CustomSerialiserFactory extends AbstractSerializerFactory
{
    private EnumSerialiser enumSerialiser = new EnumSerialiser();
    private EnumDeserialiser enumDeserialiser = new EnumDeserialiser();

    public Serializer getSerializer(Class cl) throws HessianProtocolException
    {
        if (Enum.class.isAssignableFrom(cl))
        {
            return enumSerialiser;
        }

        return null;
    }

    public Deserializer getDeserializer(Class cl)
    {
        if (Enum.class.isAssignableFrom(cl))
        {
            return enumDeserialiser;
        }

        return null;
    }
}