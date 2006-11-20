package com.zutubi.pulse.security.ldap;

import com.zutubi.pulse.model.AcegiUser;
import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;

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

    List<Group> testAuthenticate(String hostUrl, String baseDn, String managerDn, String managerPassword, String userFilter,
                                  String groupDn, String groupFilter, String groupRoleAttribute, boolean groupSearchSubtree, boolean escapeSpaces,
                                  String testLogin, String testPassword);

}
