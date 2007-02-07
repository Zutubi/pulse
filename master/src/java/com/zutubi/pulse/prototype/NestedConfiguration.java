package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
@SymbolicName("nestedConfig")
public class NestedConfiguration
{
    private String name;
    private String email;
    private String blah;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getBlah()
    {
        return blah;
    }

    public void setBlah(String blah)
    {
        this.blah = blah;
    }
}
