package com.zutubi.prototype.type;

import java.util.Map;

/**
 *
 *
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

    public void setValue(Object instance, Object value) throws Exception
    {
        // get extensions map from instance.
        Map<String, Object> extensions = ((Extendable)instance).getExtensions();

        extensions.put(getName(), value);
    }

    public boolean isReadable()
    {
        return true;
    }

    public boolean isWriteable()
    {
        return true;
    }
}
