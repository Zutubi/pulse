package com.zutubi.prototype.config.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 */
@SymbolicName("Circular")
public class CircularObject
{
    private CircularObject nested;

    public CircularObject getNested()
    {
        return nested;
    }

    public void setNested(CircularObject nested)
    {
        this.nested = nested;
    }
}
