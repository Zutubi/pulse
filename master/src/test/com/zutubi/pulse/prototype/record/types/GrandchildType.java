package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 */
@SymbolicName("grandchild")
public class GrandchildType extends ChildType1
{
    private long someLong = 1;

    public GrandchildType()
    {
    }

    public GrandchildType(int someInt, String someString, long someLong)
    {
        super(someInt, someString);
        this.someLong = someLong;
    }

    public long getSomeLong()
    {
        return someLong;
    }

    public void setSomeLong(long someLong)
    {
        this.someLong = someLong;
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
        if (!super.equals(o))
        {
            return false;
        }

        GrandchildType that = (GrandchildType) o;
        return someLong == that.someLong;

    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) (someLong ^ (someLong >>> 32));
        return result;
    }
}
