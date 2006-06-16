package com.zutubi.pulse.security.ldap;

import com.zutubi.pulse.model.User;

/**
 */
public interface LdapManager
{
    void init();

    void connect();

    public User authenticate(String username, String password);

    boolean canAutoAdd();

    String getStatusMessage();

    void test(String hostUrl, String baseDn, String managerDn, String managerPassword);
}
