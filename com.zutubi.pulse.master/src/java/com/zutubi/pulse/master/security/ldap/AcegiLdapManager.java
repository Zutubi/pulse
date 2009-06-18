package com.zutubi.pulse.master.security.ldap;

import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.pulse.master.license.LicenseHolder;
import com.zutubi.pulse.master.security.AcegiUser;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.tove.config.admin.LDAPConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.config.events.PostSaveEvent;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.ldap.DefaultInitialDirContextFactory;
import org.acegisecurity.ldap.search.FilterBasedLdapUserSearch;
import org.acegisecurity.providers.ldap.authenticator.BindAuthenticator;
import org.acegisecurity.providers.ldap.populator.DefaultLdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import java.util.*;

/**
 */
public class AcegiLdapManager implements LdapManager, ConfigurationEventListener, com.zutubi.events.EventListener
{
    private static final Logger LOG = Logger.getLogger(AcegiLdapManager.class);

    private static final String EMAIL_CONTACT_NAME = "LDAP email";

    private boolean initialised = false;
    private ConfigurationProvider configurationProvider;
    private boolean enabled = false;
    private DefaultInitialDirContextFactory contextFactory;
    private BindAuthenticator authenticator;
    private DefaultLdapAuthoritiesPopulator populator = null;
    private boolean autoAdd = false;
    private String statusMessage = null;
    private String emailAttribute = null;
    private UserManager userManager;
    private Map<String, LdapUserDetails> detailsMap = new HashMap<String, LdapUserDetails>();

    private void registerConfigListeners(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
        configurationProvider.registerEventListener(this, false, false, LDAPConfiguration.class);
    }

    private synchronized void init(LDAPConfiguration ldapConfiguration)
    {
        initialised = false;
        statusMessage = null;

        enabled = ldapConfiguration != null && ldapConfiguration.isEnabled();

        if (enabled)
        {
            autoAdd = ldapConfiguration.getAutoAddUsers();
            emailAttribute = ldapConfiguration.getEmailAttribute();

            String hostUrl = ldapConfiguration.getLdapUrl();
            String baseDn = ldapConfiguration.getBaseDn();
            String managerDn = ldapConfiguration.getManagerDn();
            String managerPassword = ldapConfiguration.getManagerPassword();
            boolean followReferrals = ldapConfiguration.getFollowReferrals();
            boolean escapeSpaces = ldapConfiguration.getEscapeSpaceCharacters();

            contextFactory = createContextFactory(hostUrl, baseDn, managerDn, managerPassword, followReferrals, escapeSpaces);
            authenticator = createAuthenticator(ldapConfiguration.getUserBaseDn(), ldapConfiguration.getUserFilter(), contextFactory);

            try
            {
                authenticator.afterPropertiesSet();
            }
            catch (Exception e)
            {
                LOG.error(e);
                statusMessage = e.getMessage();
                return;
            }

            if (TextUtils.stringSet(ldapConfiguration.getGroupBaseDn()))
            {
                populator = createPopulator(ldapConfiguration.getGroupBaseDn(), ldapConfiguration.getGroupSearchFilter(), ldapConfiguration.getGroupRoleAttribute(), ldapConfiguration.getSearchGroupSubtree(), escapeSpaces, contextFactory);
            }
        }
        else
        {
            contextFactory = null;
            authenticator = null;
            populator = null;
        }

        initialised = true;
    }

    private DefaultInitialDirContextFactory createContextFactory(String hostUrl, String baseDn, String managerDn, String managerPassword, boolean followReferrals, boolean escapeSpaces)
    {
        if (escapeSpaces)
        {
            baseDn = escapeSpaces(baseDn);
            managerDn = escapeSpaces(managerDn);
        }


        if (!hostUrl.endsWith("/"))
        {
            hostUrl += '/';
        }

        DefaultInitialDirContextFactory result = new DefaultInitialDirContextFactory(hostUrl + baseDn);

        if (TextUtils.stringSet(managerDn))
        {
            result.setManagerDn(managerDn);

            if (TextUtils.stringSet(managerPassword))
            {
                result.setManagerPassword(managerPassword);
            }
        }

        if(followReferrals)
        {
            Map<String, String> vars = new HashMap<String, String>();
            vars.put("java.naming.referral", "follow");
            result.setExtraEnvVars(vars);
        }

        return result;
    }

    private BindAuthenticator createAuthenticator(String userBase, String userFilter, DefaultInitialDirContextFactory contextFactory)
    {
        FilterBasedLdapUserSearch search = new FilterBasedLdapUserSearch(userBase, convertUserFilter(userFilter), contextFactory);
        search.setSearchSubtree(true);

        BindAuthenticator authenticator = new BindAuthenticator(contextFactory);
        authenticator.setUserSearch(search);
        return authenticator;
    }

    private DefaultLdapAuthoritiesPopulator createPopulator(String groupDn, String groupFilter, String groupRoleAttribute, boolean searchSubtree, boolean escapeSpaces, DefaultInitialDirContextFactory contextFactory)
    {
        if (escapeSpaces)
        {
            groupDn = escapeSpaces(groupDn);
        }

        DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(contextFactory, groupDn);
        if (TextUtils.stringSet(groupFilter))
        {
            populator.setGroupSearchFilter(convertGroupFilter(groupFilter));
        }

        if (TextUtils.stringSet(groupRoleAttribute))
        {
            populator.setGroupRoleAttribute(groupRoleAttribute);
        }

        populator.setSearchSubtree(searchSubtree);
        populator.setRolePrefix("");
        populator.setConvertToUpperCase(false);
        return populator;
    }

    private String convertUserFilter(String userFilter)
    {
        return userFilter.replace("${login}", "{0}");
    }

    private String convertGroupFilter(String groupFilter)
    {
        return groupFilter.replace("${user.dn}", "{0}").replace("${login}", "{1}");
    }

    public synchronized void connect()
    {
        statusMessage = null;
        try
        {
            contextFactory.newInitialDirContext();
        }
        catch (Exception e)
        {
            LOG.warning("Unable to connect to LDAP server: " + e.getMessage(), e);
            statusMessage = e.getMessage();
        }
    }

    public synchronized UserConfiguration authenticate(String username, String password, boolean addContact)
    {
        if (enabled && initialised)
        {
            try
            {
                LdapUserDetails details = ldapAuthenticate(authenticator, username, password);
                String name = getStringAttribute(details, "cn", username);
                if (name == null)
                {
                    name = username;
                }

                UserConfiguration user = userManager.getUserConfig(username);
                if(user == null)
                {
                    user = new UserConfiguration(username, name);
                    user.setAuthenticatedViaLdap(true);
                }

                if (addContact && TextUtils.stringSet(emailAttribute))
                {
                    addContact(user, details);
                }

                detailsMap.put(username, details);
                return user;
            }
            catch (BadCredentialsException e)
            {
                LOG.info("LDAP login failure: user: " + username + " : " + e.getMessage(), e);
            }
            catch (Exception e)
            {
                LOG.warning("Error contacting LDAP server: " + e.getMessage(), e);
                statusMessage = e.getMessage();
            }
        }

        return null;
    }

    private LdapUserDetails ldapAuthenticate(BindAuthenticator authenticator, String username, String password)
    {
        if(!TextUtils.stringSet(password))
        {
            throw new BadCredentialsException("LDAP users cannot have an empty password");
        }
        return authenticator.authenticate(username, password);
    }

    public synchronized void addLdapRoles(AcegiUser user)
    {
        if (populator != null)
        {
            LdapUserDetails details = detailsMap.get(user.getUsername());
            if (details != null)
            {
                try
                {
                    List<UserGroupConfiguration> groups = getLdapGroups(details, populator);
                    for(UserGroupConfiguration group: groups)
                    {
                        LOG.debug("Adding user '" + details.getUsername() + "' to group '" + group.getName() + "' via LDAP");
                        user.addGroup(group);
                    }
                }
                catch (Exception e)
                {
                    LOG.severe("Error retrieving group roles from LDAP server: " + e.getMessage(), e);
                }
            }
        }
    }

    private List<UserGroupConfiguration> getLdapGroups(LdapUserDetails details, DefaultLdapAuthoritiesPopulator populator)
    {
        List<UserGroupConfiguration> groups = new LinkedList<UserGroupConfiguration>();
        GrantedAuthority[] ldapAuthorities = populator.getGrantedAuthorities(details);
        for (GrantedAuthority authority : ldapAuthorities)
        {
            UserGroupConfiguration group = userManager.getGroupConfig(authority.getAuthority());
            if (group != null)
            {
                groups.add(group);
            }
        }

        return groups;
    }

    private void addContact(UserConfiguration user, LdapUserDetails details)
    {
        UserPreferencesConfiguration prefs = user.getPreferences();
        if (!prefs.getContacts().containsKey(EMAIL_CONTACT_NAME))
        {
            String email = getStringAttribute(details, emailAttribute, user.getLogin());
            if (email != null)
            {
                try
                {
                    new InternetAddress(email);
                    EmailContactConfiguration contact = new EmailContactConfiguration();
                    contact.setName(EMAIL_CONTACT_NAME);
                    contact.setAddress(email);
                    prefs.getContacts().put(EMAIL_CONTACT_NAME, contact);
                }
                catch (AddressException e)
                {
                    LOG.warning("Ignoring invalid email address '" + email + "' for user '" + user.getLogin() + "'");
                }
            }
        }
    }

    private String getStringAttribute(LdapUserDetails details, String attribute, String username)
    {
        Attribute att = details.getAttributes().get(attribute);
        if (att != null)
        {
            try
            {
                Object value = att.get();
                if (value instanceof String)
                {
                    return (String) value;
                }
            }
            catch (NamingException e)
            {
                LOG.debug("Unable to get attribute '" + attribute + "' for user '" + username + "': " + e.getMessage(), e);
            }
        }
        else
        {
            LOG.debug("User '" + username + "' has no '" + attribute + "' attribute");
        }

        return null;
    }

    public synchronized boolean canAutoAdd()
    {
        return enabled && initialised && autoAdd && LicenseHolder.hasAuthorization("canAddUser");
    }

    public List<UserGroupConfiguration> testAuthenticate(LDAPConfiguration configuration, String testLogin, String testPassword)
    {
        DefaultInitialDirContextFactory contextFactory = createContextFactory(configuration.getLdapUrl(), configuration.getBaseDn(), configuration.getManagerDn(), configuration.getManagerPassword(), configuration.getFollowReferrals(), configuration.getEscapeSpaceCharacters());
        contextFactory.newInitialDirContext();

        BindAuthenticator authenticator = createAuthenticator(configuration.getUserBaseDn(), configuration.getUserFilter(), contextFactory);
        LdapUserDetails details = ldapAuthenticate(authenticator, testLogin, testPassword);

        if(TextUtils.stringSet(configuration.getGroupBaseDn()))
        {
            DefaultLdapAuthoritiesPopulator populator = createPopulator(configuration.getGroupBaseDn(), configuration.getGroupSearchFilter(), configuration.getGroupRoleAttribute(), configuration.getSearchGroupSubtree(), configuration.getEscapeSpaceCharacters(), contextFactory);
            return getLdapGroups(details, populator);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private String escapeSpaces(String dn)
    {
        return dn.replaceAll(" ", "\\\\20");
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public synchronized void handleConfigurationEvent(ConfigurationEvent event)
    {
        if(event instanceof PostSaveEvent)
        {
            init((LDAPConfiguration) event.getInstance());
        }
    }

    public void handleEvent(Event event)
    {
        if (event instanceof ConfigurationEventSystemStartedEvent)
        {
            registerConfigListeners(((ConfigurationEventSystemStartedEvent)event).getConfigurationProvider());
        }
        else
        {
            init(configurationProvider.get(LDAPConfiguration.class));
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationEventSystemStartedEvent.class, ConfigurationSystemStartedEvent.class };
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
