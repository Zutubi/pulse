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
    private Map<String, RecordTypeInfo> nameToInfo = new HashMap<String, RecordTypeInfo>();
    private Map<Class, RecordTypeInfo> typeToInfo = new HashMap<Class, RecordTypeInfo>();

    public void register(String symbolicName, Class type) throws InvalidRecordTypeException
    {
        RecordTypeInfo info = new RecordTypeInfo(symbolicName, type);
        nameToInfo.put(symbolicName, info);
        typeToInfo.put(type, info);
    }

    public void register(Class type) throws InvalidRecordTypeException
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
        return nameToInfo.get(symbolicName).getType();
    }

    public RecordTypeInfo getInfo(String symbolicName)
    {
        return nameToInfo.get(symbolicName);
    }

    public String getSymbolicName(Class type)
    {
        return typeToInfo.get(type).getSymbolicName();
    }

    public RecordTypeInfo getInfo(Class type)
    {
        return typeToInfo.get(type);
    }
}
