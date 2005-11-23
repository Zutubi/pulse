package com.cinnamonbob.hessian;

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;

import java.io.IOException;

/**
 * Deserialises Java 1.5 enumerations, because hessian barfs on them itself.
 *
 * Adapted from an implementation posted to hessian-interest by Jason Stiefel.
 */
public class EnumDeserialiser extends AbstractDeserializer
{
    public Object readMap(AbstractHessianInput in) throws IOException
    {
        String clazz = null;
        String value = null;

        while (!in.isEnd())
        {
            clazz = in.readString();
            value = in.readString();
        }
        in.readMapEnd();

        if (clazz == null)
        {
            throw new IOException("Expected Enum class name");
        }

        if (value == null)
        {
            throw new IOException("Expected Enum class value");
        }

        /* If the enum type is an inner class, we need to strip the $n. */
        int index = clazz.lastIndexOf('$');
        if(index != -1)
        {
            clazz = clazz.substring(0, index);
        }

        Class enumClazz = null;
        try
        {
            enumClazz = Class.forName(clazz);
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("Expected Enum class (" + clazz + ") was not found");
        }

        return Enum.valueOf(enumClazz, value);
    }
}

