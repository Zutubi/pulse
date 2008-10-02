package com.zutubi.pulse.security.ldap;

import com.zutubi.pulse.master.model.AcegiUser;
import com.zutubi.pulse.tove.config.admin.LDAPConfiguration;
import com.zutubi.pulse.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.tove.config.user.UserConfiguration;

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

    List<GroupConfiguration> testAuthenticate(LDAPConfiguration configuration, String testLogin, String testPassword);

}
