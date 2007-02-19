package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 * @deprecated
 */
@SymbolicName("child1")
public class ChildType1 extends ParentType
{
    private String someString;

    public ChildType1()
    {
    }

    public ChildType1(int someInt, String someString)
    {
        super(someInt);
        this.someString = someString;
    }

    public String getSomeString()
    {
        return someString;
    }

    public void setSomeString(String someString)
    {
        this.someString = someString;
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

        ChildType1 that = (ChildType1) o;
        return !(someString != null ? !someString.equals(that.someString) : that.someString != null);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (someString != null ? someString.hashCode() : 0);
        return result;
    }
}
