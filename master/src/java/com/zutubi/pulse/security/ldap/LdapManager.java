package com.zutubi.pulse.security.ldap;

import com.zutubi.pulse.model.AcegiUser;
import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.prototype.config.admin.LDAPConfiguration;

import java.util.List;

/**
 */
public interface LdapManager
{
    void init();

    void connect();

    public User authenticate(String username, String password);

    public void addLdapRoles(AcegiUser user);

    boolean canAutoAdd();

    String getStatusMessage();

    List<Group> testAuthenticate(LDAPConfiguration configuration, String testLogin, String testPassword);

}
