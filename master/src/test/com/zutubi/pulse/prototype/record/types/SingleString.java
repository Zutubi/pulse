package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 */
@SymbolicName("ss")
public class SingleString
{
    private String value;

    public SingleString()
    {
    }

    public SingleString(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }


    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof SingleString))
        {
            return false;
        }

        return value.equals(((SingleString)obj).value);
    }
}
