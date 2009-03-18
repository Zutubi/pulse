package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.SymbolicName;

/**
 * An extension of the extendable configuration.
 */
@SymbolicName("extensionOne")
public class ExtensionOneConfiguration extends ExtendableConfiguration
{
    public ExtensionOneConfiguration()
    {
    }

    public ExtensionOneConfiguration(String name)
    {
        super(name);
    }
}
