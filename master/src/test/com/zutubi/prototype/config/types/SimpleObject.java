package com.zutubi.prototype.config.types;

import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.prototype.annotation.ID;

/**
 */
@SymbolicName("Simple")
public class SimpleObject
{
    @ID
    private String strA;

    private String strB;

    public String getStrA()
    {
        return strA;
    }

    public void setStrA(String strA)
    {
        this.strA = strA;
    }

    public String getStrB()
    {
        return strB;
    }

    public void setStrB(String strB)
    {
        this.strB = strB;
    }
}
