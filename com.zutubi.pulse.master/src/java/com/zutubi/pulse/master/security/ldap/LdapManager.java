package com.zutubi.pulse.master.security.ldap;

import com.zutubi.pulse.master.security.AcegiUser;
import com.zutubi.pulse.master.tove.config.admin.LDAPConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

import java.util.List;

/**
 */
public interface LdapManager
{
    void connect();

    public UserConfiguration authenticate(String username, String password, boolean addContact);

    public void addLdapRoles(AcegiUser user);

    boolean canAutoAdd();

    String getStatusMessage();

    List<UserGroupConfiguration> testAuthenticate(LDAPConfiguration configuration, String testLogin, String testPassword);

}
