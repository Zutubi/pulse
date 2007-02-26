package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Url;
import com.zutubi.prototype.annotation.Password;
import com.zutubi.prototype.annotation.Form;
import com.zutubi.prototype.annotation.ConfigurationCheck;

/**
 *
 *
 */
@Form(fieldOrder={"enabled", "ldapUrl", "baseDn", "managerDn", "managerPassword",
        "userFilter", "autoAddUsers", "emailAttribute", "groupRootDn", "groupSearchFilter",
        "groupNameAttribute", "searchGroupSubtree", "escapeSpaceCharacters"})
@ConfigurationCheck(LDAPConfigurationCheckHandler.class)
public class LDAPConfiguration
{
    private boolean enabled;
    private String ldapUrl;
    private String baseDn;

    private String managerDn;
    private String managerPassword;

    private String userFilter;
    private boolean autoAddUsers;

    private String emailAttribute;

    private String groupRootDn;
    private String groupSearchFilter;
    private String groupNameAttribute;
    private boolean searchGroupSubtree;
    private boolean escapeSpaceCharacters;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Required()
    @Url()
    public String getLdapUrl()
    {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl)
    {
        this.ldapUrl = ldapUrl;
    }

    @Required()
    public String getBaseDn()
    {
        return baseDn;
    }

    public void setBaseDn(String baseDn)
    {
        this.baseDn = baseDn;
    }

    public String getManagerDn()
    {
        return managerDn;
    }

    public void setManagerDn(String managerDn)
    {
        this.managerDn = managerDn;
    }

    @Password()
    public String getManagerPassword()
    {
        return managerPassword;
    }

    public void setManagerPassword(String managerPassword)
    {
        this.managerPassword = managerPassword;
    }

    @Required()
    public String getUserFilter()
    {
        return userFilter;
    }

    public void setUserFilter(String userFilter)
    {
        this.userFilter = userFilter;
    }

    public boolean isAutoAddUsers()
    {
        return autoAddUsers;
    }

    public void setAutoAddUsers(boolean autoAddUsers)
    {
        this.autoAddUsers = autoAddUsers;
    }

    public String getEmailAttribute()
    {
        return emailAttribute;
    }

    public void setEmailAttribute(String emailAttribute)
    {
        this.emailAttribute = emailAttribute;
    }

    public String getGroupRootDn()
    {
        return groupRootDn;
    }

    public void setGroupRootDn(String groupRootDn)
    {
        this.groupRootDn = groupRootDn;
    }

    public String getGroupSearchFilter()
    {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(String groupSearchFilter)
    {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getGroupNameAttribute()
    {
        return groupNameAttribute;
    }

    public void setGroupNameAttribute(String groupNameAttribute)
    {
        this.groupNameAttribute = groupNameAttribute;
    }

    public boolean isSearchGroupSubtree()
    {
        return searchGroupSubtree;
    }

    public void setSearchGroupSubtree(boolean searchGroupSubtree)
    {
        this.searchGroupSubtree = searchGroupSubtree;
    }

    public boolean isEscapeSpaceCharacters()
    {
        return escapeSpaceCharacters;
    }

    public void setEscapeSpaceCharacters(boolean escapeSpaceCharacters)
    {
        this.escapeSpaceCharacters = escapeSpaceCharacters;
    }
}
