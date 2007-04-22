package com.zutubi.prototype.config.types;

import com.zutubi.config.annotations.SymbolicName;

/**
 */
@SymbolicName("GrandParent")
public class GrandParentObject
{
    private String strA;
    private SimpleObject simple;
    private CompositeObject composite;

    public String getStrA()
    {
        return strA;
    }

    public void setStrA(String strA)
    {
        this.strA = strA;
    }

    public SimpleObject getSimple()
    {
        return simple;
    }

    public void setSimple(SimpleObject simple)
    {
        this.simple = simple;
    }

    public CompositeObject getComposite()
    {
        return composite;
    }

    public void setComposite(CompositeObject composite)
    {
        this.composite = composite;
    }
}
