package com.zutubi.pulse.prototype.config.user.contacts;

/**
 *
 *
 */
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
