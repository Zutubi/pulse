package com.zutubi.pulse.hessian;

import com.caucho.hessian.io.*;

import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * A serialiser factory that handles Java 5 enums.
 *
 * Adapted from an implementation posted to hessian-interest by Jason Stiefel.
 */
public class CustomSerialiserFactory extends AbstractSerializerFactory
{
    private EnumSerialiser enumSerialiser = new EnumSerialiser();
    private EnumDeserialiser enumDeserialiser = new EnumDeserialiser();
    private JavaDeserializer logRecordDeserializer = new CustomDeserialiser(LogRecord.class);
    private JavaDeserializer logLevelDeserializer = new CustomDeserialiser(Level.class);

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
        else if(LogRecord.class.isAssignableFrom(cl))
        {
            return logRecordDeserializer;
        }
        else if(Level.class.isAssignableFrom(cl))
        {
            return logLevelDeserializer;
        }

        return null;
    }
}