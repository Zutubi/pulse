package com.zutubi.pulse.master.security.ldap;

import com.zutubi.pulse.master.security.AcegiUser;
import com.zutubi.pulse.master.tove.config.admin.LDAPConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

import java.util.List;

/**
 * The base interface for the Pulse interface to ldap.
 */
public interface LdapManager
{
    /**
     * Authenticate the credentials, returning the user if authentication is successful.
     *
     * @param username      the username of the user being authenticated
     * @param password      the password of the user being authenticated
     * @param addContact    if true, the ldap manager will add an email contact point to the
     * user instance configured using the users email address retrieved via ldap.  The contact
     * point name will be 'LDAP email'
     * 
     * @return  a user instance if authentication was successful, null otherwise.  If an exception
     * is generated during authentication, the message will be available via {@link #getStatusMessage()} 
     */
    public UserConfiguration authenticate(String username, String password, boolean addContact);

    /**
     * This method will add the {@link GroupConfiguration} instances to the user via {@link AcegiUser#addGroup(GroupConfiguration)}
     * for any of the users ldap group memberships where there exists a corresponding group within Pulse.  
     *
     * This requires ldap group integration to be configured.
     *
     * @param user  the user whose groups will be queried.
     */
    public void addLdapRoles(AcegiUser user);

    /**
     * Returns true if ldap users are allowed to be automatically added to Pulse.
     *
     * @return true if a user authenticated via ldap can be added to Pulse, false otherwise.
     */
    boolean canAutoAdd();

    /**
     * The status message contains details of the latest internal error that may
     * have occured.  
     *
     * @return the message, null if no message exists.
     */
    String getStatusMessage();

    /**
     * This method tests whether the test credentials can be used to connect to the ldap configuration.
     *
     * @param configuration the ldap configuration being tested
     * @param testLogin     the username to be used for the test authentication
     * @param testPassword  the password to be used for the test authentication
     *
     * @return a list of local groups that the authenticated user belongs to.
     */
    List<UserGroupConfiguration> testAuthenticate(LDAPConfiguration configuration, String testLogin, String testPassword);

}
