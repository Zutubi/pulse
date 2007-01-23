package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 */
@SymbolicName("Boy George")
public class SingleReference
{
    private SingleString singleString;

    public SingleReference()
    {
    }

    public SingleReference(String s)
    {
        singleString = new SingleString(s);
    }

    public SingleString getSingleString()
    {
        return singleString;
    }

    public void setSingleString(SingleString singleString)
    {
        this.singleString = singleString;
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

        SingleReference that = (SingleReference) o;

        if (singleString != null ? !singleString.equals(that.singleString) : that.singleString != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (singleString != null ? singleString.hashCode() : 0);
    }
}
