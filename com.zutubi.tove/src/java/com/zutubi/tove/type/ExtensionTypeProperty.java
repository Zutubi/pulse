package com.zutubi.tove.type;

import java.util.Map;

/**
 * A property that is not statically declared in code, but rather added at
 * run time.  This allows a type to be extended beyond the fields in the
 * original compiled code.
 */
public class ExtensionTypeProperty extends TypeProperty
{
    public ExtensionTypeProperty(String name, Type type)
    {
        super(name, type);
    }

    public Object getValue(Object instance) throws Exception
    {
        // get extensions map from instance.
        Map<String, Object> extensions = ((Extendable)instance).getExtensions();
        
        return extensions.get(getName());
    }

    public void setValue(Object instance, Object value)
    {
        // get extensions map from instance.
        Map<String, Object> extensions = ((Extendable)instance).getExtensions();
        extensions.put(getName(), value);
    }

    public boolean isReadable()
    {
        return true;
    }

    public boolean isWritable()
    {
        return true;
    }
}
