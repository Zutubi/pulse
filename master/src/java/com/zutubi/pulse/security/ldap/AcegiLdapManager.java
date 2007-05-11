package com.zutubi.pulse.security.ldap;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationEventListener;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.prototype.config.admin.LDAPConfiguration;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.ldap.DefaultInitialDirContextFactory;
import org.acegisecurity.ldap.search.FilterBasedLdapUserSearch;
import org.acegisecurity.providers.ldap.authenticator.BindAuthenticator;
import org.acegisecurity.providers.ldap.populator.DefaultLdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import java.util.*;

/**
 */
public class AcegiLdapManager implements LdapManager, ConfigurationEventListener
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

    public synchronized void init()
    {
        configurationProvider.registerEventListener(this, false, false, LDAPConfiguration.class);
        LDAPConfiguration ldapConfiguration = configurationProvider.get(LDAPConfiguration.class);

        init(ldapConfiguration);
    }

    private void init(LDAPConfiguration ldapConfiguration)
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

    public synchronized User authenticate(String username, String password)
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

                User user = new User(username, name);

                if (TextUtils.stringSet(emailAttribute))
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
                    List<Group> groups = getLdapGroups(details, populator);
                    for(Group group: groups)
                    {
                        LOG.debug("Adding user '" + details.getUsername() + "' to group '" + group.getName() + "' via LDAP");
                        for (GrantedAuthority a : group.getAuthorities())
                        {
                            user.addTransientAuthority(a.getAuthority());
                        }
                    }
                }
                catch (Exception e)
                {
                    LOG.severe("Error retrieving group roles from LDAP server: " + e.getMessage(), e);
                }
            }
        }
    }

    private List<Group> getLdapGroups(LdapUserDetails details, DefaultLdapAuthoritiesPopulator populator)
    {
        List<Group> groups = new LinkedList<Group>();
        GrantedAuthority[] ldapAuthorities = populator.getGrantedAuthorities(details);
        for (GrantedAuthority authority : ldapAuthorities)
        {
            Group group = userManager.getGroup(authority.getAuthority());
            if (group != null)
            {
                groups.add(group);
            }
        }

        return groups;
    }

    private void addContact(User user, LdapUserDetails details)
    {
        if (user.getContactPoint(EMAIL_CONTACT_NAME) == null)
        {
            String email = getStringAttribute(details, emailAttribute, user.getLogin());
            if (email != null)
            {
                EmailContactPoint point = new EmailContactPoint(email);
                point.setName(EMAIL_CONTACT_NAME);
                user.add(point);
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

    public List<Group> testAuthenticate(LDAPConfiguration configuration, String testLogin, String testPassword)
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
            return new ArrayList<Group>(0);
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
            init((LDAPConfiguration) ((PostSaveEvent)event).getNewInstance());
        }
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
