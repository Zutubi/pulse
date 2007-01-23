package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 */
@SymbolicName("thenum")
public class SingleEnum
{
    public enum Test
    {
        ONE,
        TWO,
        THREE
    }

    private Test test;

    public SingleEnum()
    {
    }

    public SingleEnum(Test test)
    {
        this.test = test;
    }

    public Test getTest()
    {
        return test;
    }

    public void setTest(Test test)
    {
        this.test = test;
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

        SingleEnum that = (SingleEnum) o;
        return test == that.test;
    }

    public int hashCode()
    {
        return (test != null ? test.hashCode() : 0);
    }
}
