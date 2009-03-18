package com.zutubi.pulse.core.marshal.types;

import com.zutubi.pulse.core.engine.api.Referenceable;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * A trivial composite type used for properties in more elaborate test types.
 */
@SymbolicName("trivial")
@Referenceable
public class TrivialConfiguration extends AbstractNamedConfiguration
{
    public TrivialConfiguration()
    {
    }

    public TrivialConfiguration(String name)
    {
        super(name);
    }
}
