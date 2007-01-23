package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 */
@SymbolicName("parent")
public class ParentType
{
    private int someInt;

    public ParentType()
    {
    }

    public ParentType(int someInt)
    {
        this.someInt = someInt;
    }

    public int getSomeInt()
    {
        return someInt;
    }

    public void setSomeInt(int someInt)
    {
        this.someInt = someInt;
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

        ParentType that = (ParentType) o;
        return someInt == that.someInt;
    }

    public int hashCode()
    {
        return someInt;
    }
}
