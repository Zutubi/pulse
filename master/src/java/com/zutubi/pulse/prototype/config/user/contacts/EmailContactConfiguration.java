package com.zutubi.pulse.prototype.config.user.contacts;

import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("zutubi.emailContactConfig")
public class EmailContactConfiguration extends ContactConfiguration
{
    private String address;

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getUid()
    {
        return getAddress();
    }
}
