package com.zutubi.pulse.prototype.config.admin;

/**
 *
 *
 */
public class LDAPConfiguration
{
    private String ldapUrl;
    private String basedn;
    private boolean enabled;

    public String getLdapUrl()
    {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl)
    {
        this.ldapUrl = ldapUrl;
    }

    public String getBasedn()
    {
        return basedn;
    }

    public void setBasedn(String basedn)
    {
        this.basedn = basedn;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
