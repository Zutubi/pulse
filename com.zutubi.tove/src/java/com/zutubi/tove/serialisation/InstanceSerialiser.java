package com.zutubi.tove.serialisation;

import com.zutubi.tove.config.api.Configuration;

import java.io.OutputStream;
import java.io.InputStream;

/**
 */
public interface InstanceSerialiser
{
    void serialise(Configuration instance, OutputStream stream);
    <T extends Configuration> T deserialise(InputStream stream, Class<T> expectedClass);
}
