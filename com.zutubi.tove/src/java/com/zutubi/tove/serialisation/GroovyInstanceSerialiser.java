package com.zutubi.tove.serialisation;

import com.zutubi.tove.config.api.Configuration;

import java.io.OutputStream;
import java.io.InputStream;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 */
public class GroovyInstanceSerialiser implements InstanceSerialiser
{
    public void serialise(Configuration instance, OutputStream stream)
    {
        throw new RuntimeException("Not implemented");
    }

    public <T extends Configuration> T deserialise(InputStream stream, Class<T> expectedClass)
    {
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
        return expectedClass.cast(shell.evaluate(stream));
    }
}
