package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 */
@SymbolicName("polycat")
public class PolymorphicReference
{
    private ParentType pt;

    public PolymorphicReference()
    {
    }

    public PolymorphicReference(ParentType pt)
    {
        this.pt = pt;
    }

    public ParentType getPt()
    {
        return pt;
    }

    public void setPt(ParentType pt)
    {
        this.pt = pt;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        PolymorphicReference that = (PolymorphicReference) o;
        return !(pt != null ? !pt.equals(that.pt) : that.pt != null);
    }

    public int hashCode()
    {
        return (pt != null ? pt.hashCode() : 0);
    }
}
