package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.USERS_SCOPE;
import com.zutubi.pulse.master.tove.config.admin.LDAPConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.acceptance.pages.admin.GroupsPage;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlrpc.XmlRpcException;

import static java.util.Arrays.asList;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Acceptance test for the ldap integration.
 */
public class LdapAcceptanceTest extends SeleniumTestBase
{
    private static final String LDAP_CONFIG_PATH = "settings/ldap";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        deleteAllUsers();
        setAddUserViaLdap(false);
    }

    public void testAuthenticationViaLdap() throws Exception
    {
        assertFalse(browser.login("user1", "user1"));
        addUser("user1", true);
        assertTrue(browser.login("user1", "user1"));
    }

    public void testAuthenticateXmlRpcViaLdap() throws Exception
    {
        try
        {
            xmlRpcHelper.login("user2", "user2");
            fail();
        }
        catch (XmlRpcException e)
        {
            assertTrue(e.getMessage().contains("Bad credentials"));
        }
        addUser("user2", true);
        assertNotNull(xmlRpcHelper.login("user2", "user2"));
        xmlRpcHelper.logout();
    }

    public void testAddUserViaLdap() throws Exception
    {
        assertFalse(browser.login("user3", "user3"));
        setAddUserViaLdap(true);
        assertTrue(browser.login("user3", "user3"));
    }

    public void testEmailContactViaLdap() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();

        setAddUserViaLdap(true);
        assertTrue(browser.login("user3", "user3"));
        try
        {
            xmlRpcHelper.getConfig(getPath(USERS_SCOPE, "user3", "preferences", "contacts", "LDAP email"));
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("does not exist"));
        }

        setEmailAttribute("mail");
        assertTrue(browser.login("user4", "user4"));

        Hashtable<String, Object> contact = xmlRpcHelper.getConfig(getPath(USERS_SCOPE, "user4", "preferences", "contacts", "LDAP email"));
        assertEquals("mborn@example.com", contact.get("address"));
        
        xmlRpcHelper.logout();
    }

    public void testGroupsViaLdap() throws Exception
    {
        setAddUserViaLdap(true);
        setGroupNameAttribute("cn");
        setGroupSearchFilter("(uniquemember=${user.dn})");
        assertTrue(browser.login("user2", "user2"));

        // user 2 is an administrator in ldap, so should be able to view the admin pages.
        GroupsPage page = browser.openAndWaitFor(GroupsPage.class);
        assertTrue(page.isPresent());
    }

    
/* CIB-2556
    public void testFollowReferrals() throws Exception
    {
        setAddUserViaLdap(true);
        setFollowReferrals(false);
        assertFalse(browser.login("user7", "user7"));
        setFollowReferrals(true);
        assertTrue(browser.login("user8", "user8"));
    }
*//*
    public void testUseTls()
    {
        // this is currently a system property setting so needs to be altered before
        // we can do acceptance testing on it.
    }
*/

    private void deleteAllUsers() throws Exception
    {
        // admin is not deleted because it is a permanent configuration.
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.deleteAllConfigs(getPath(USERS_SCOPE, WILDCARD_ANY_ELEMENT));
        xmlRpcHelper.logout();
    }

    private void addUser(String login, boolean authenticateViaLdap) throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        Hashtable<String, Object> user = xmlRpcHelper.createDefaultConfig(UserConfiguration.class);
        user.put("login", login);
        user.put("name", login);
        user.put("authenticatedViaLdap", authenticateViaLdap);
        xmlRpcHelper.insertConfig(USERS_SCOPE, user);
        xmlRpcHelper.logout();
    }

    private void setGroupNameAttribute(String s) throws Exception
    {
        setLdapProperty("groupRoleAttribute", s);
    }

    private void setGroupSearchFilter(String s) throws Exception
    {
        setLdapProperty("groupSearchFilter", s);
    }

    private void setFollowReferrals(boolean b) throws Exception
    {
        setLdapProperty("followReferrals", b);
    }

    private void setAddUserViaLdap(boolean b) throws Exception
    {
        setLdapProperty("autoAddUsers", b);
    }

    private void setEmailAttribute(String s) throws Exception
    {
        setLdapProperty("emailAttribute", s);
    }

    private void setLdapProperty(String propertyName, Object value) throws Exception
    {
        boolean loggedIn = xmlRpcHelper.isLoggedIn();
        if (!loggedIn)
        {
            xmlRpcHelper.loginAsAdmin();
        }
        Hashtable<String, Object> ldapConfig = xmlRpcHelper.getConfig(LDAP_CONFIG_PATH);
        ldapConfig.put(propertyName, value);
        xmlRpcHelper.saveConfig(LDAP_CONFIG_PATH, ldapConfig, false);
        if (!loggedIn)
        {
            xmlRpcHelper.logout();
        }
    }


}
