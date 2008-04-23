package com.zutubi.pulse.security.ldap;

import com.zutubi.pulse.model.AcegiUser;
import com.zutubi.pulse.prototype.config.admin.LDAPConfiguration;
import com.zutubi.pulse.prototype.config.group.GroupConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

import java.util.List;

/**
 */
public interface LdapManager
{
    void connect();

    public UserConfiguration authenticate(String username, String password);

    public void addLdapRoles(AcegiUser user);

    boolean canAutoAdd();

    String getStatusMessage();

    List<GroupConfiguration> testAuthenticate(LDAPConfiguration configuration, String testLogin, String testPassword);

}
