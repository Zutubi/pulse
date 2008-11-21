package com.zutubi.tove.config.types;

import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 */
@SymbolicName("Simple")
public class SimpleObject extends AbstractConfiguration
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
