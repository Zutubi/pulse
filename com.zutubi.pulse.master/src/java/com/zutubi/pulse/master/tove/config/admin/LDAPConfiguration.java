/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

import java.util.List;

/**
 * Configuration for LDAP auth integration.
 */
@SymbolicName("zutubi.ldapConfig")
@Form(fieldOrder = {"enabled", "ldapUrl", "baseDn", "managerDn", "managerPassword",
        "userBaseDn", "userFilter", "autoAddUsers", "passwordAttribute", "emailAttribute", "groupBaseDns",
        "groupSearchFilter", "groupRoleAttribute", "searchGroupSubtree", "followReferrals", "escapeSpaceCharacters"})
@Classification(single = "security")
public class LDAPConfiguration extends AbstractConfiguration
{
    @ControllingCheckbox
    private boolean enabled;
    @Required
    private String ldapUrl;
    @Required
    private String baseDn;

    private String managerDn;
    @Password
    private String managerPassword;

    private String userBaseDn;
    @Required
    private String userFilter;
    private boolean autoAddUsers;
    private String passwordAttribute;
    private String emailAttribute;

    @StringList
    private List<String> groupBaseDns;
    private String groupSearchFilter;
    private String groupRoleAttribute;
    private boolean searchGroupSubtree;

    private boolean escapeSpaceCharacters;
    private boolean followReferrals;

    public LDAPConfiguration()
    {
        setPermanent(true);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getLdapUrl()
    {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl)
    {
        this.ldapUrl = ldapUrl;
    }

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

    public String getManagerPassword()
    {
        return managerPassword;
    }

    public void setManagerPassword(String managerPassword)
    {
        this.managerPassword = managerPassword;
    }

    public String getUserBaseDn()
    {
        return userBaseDn;
    }

    public void setUserBaseDn(String userBaseDn)
    {
        this.userBaseDn = userBaseDn;
    }

    public String getUserFilter()
    {
        return userFilter;
    }

    public void setUserFilter(String userFilter)
    {
        this.userFilter = userFilter;
    }

    public boolean getAutoAddUsers()
    {
        return autoAddUsers;
    }

    public void setAutoAddUsers(boolean autoAddUsers)
    {
        this.autoAddUsers = autoAddUsers;
    }

    public String getPasswordAttribute()
    {
        return passwordAttribute;
    }

    public void setPasswordAttribute(String passwordAttribute)
    {
        this.passwordAttribute = passwordAttribute;
    }

    public String getEmailAttribute()
    {
        return emailAttribute;
    }

    public void setEmailAttribute(String emailAttribute)
    {
        this.emailAttribute = emailAttribute;
    }

    public List<String> getGroupBaseDns()
    {
        return groupBaseDns;
    }

    public void setGroupBaseDns(List<String> groupBaseDns)
    {
        this.groupBaseDns = groupBaseDns;
    }

    public String getGroupSearchFilter()
    {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(String groupSearchFilter)
    {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getGroupRoleAttribute()
    {
        return groupRoleAttribute;
    }

    public void setGroupRoleAttribute(String groupRoleAttribute)
    {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public boolean getSearchGroupSubtree()
    {
        return searchGroupSubtree;
    }

    public void setSearchGroupSubtree(boolean searchGroupSubtree)
    {
        this.searchGroupSubtree = searchGroupSubtree;
    }

    public boolean getFollowReferrals()
    {
        return followReferrals;
    }

    public void setFollowReferrals(boolean followReferrals)
    {
        this.followReferrals = followReferrals;
    }

    public boolean getEscapeSpaceCharacters()
    {
        return escapeSpaceCharacters;
    }

    public void setEscapeSpaceCharacters(boolean escapeSpaceCharacters)
    {
        this.escapeSpaceCharacters = escapeSpaceCharacters;
    }
}
