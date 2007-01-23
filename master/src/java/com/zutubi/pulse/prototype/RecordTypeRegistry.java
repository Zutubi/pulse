package com.zutubi.pulse.prototype;

import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.prototype.record.SymbolicName;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry that maps from record symbolic names to classes and vice-versa.
 * It is used to store the type information with a record without tying it to
 * a specific class - the symbolic name adds a level of indirection so that
 * the class can change over time.
 */
public class RecordTypeRegistry
{
    private Map<String, Class> nameToType = new HashMap<String, Class>();
    private Map<Class, String> typeToName = new HashMap<Class, String>();

    public void register(String symbolicName, Class type)
    {
        nameToType.put(symbolicName, type);
        typeToName.put(type, symbolicName);
    }

    public void register(Class type)
    {
        // TODO: why does Intellij insist on this cast?
        SymbolicName a = (SymbolicName) type.getAnnotation(SymbolicName.class);
        if(a == null)
        {
            throw new PulseRuntimeException("Unable to register class '" + type + "': no SymbolicName annotation");
        }

        register(a.value(), type);
    }

    public Class getType(String symbolicName)
    {
        return nameToType.get(symbolicName);
    }

    public String getSymbolicName(Class type)
    {
        return typeToName.get(type);
    }
}
