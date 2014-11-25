package com.zutubi.pulse.master.security.ldap;

import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.license.LicenseHolder;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.security.Principle;
import com.zutubi.pulse.master.tove.config.admin.LDAPConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.config.events.PostSaveEvent;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.*;

import static com.zutubi.pulse.master.license.LicenseHolder.AUTH_ADD_USER;

/**
 * LDAP integration.
 */
public class AcegiLdapManager implements LdapManager, ConfigurationEventListener, com.zutubi.events.EventListener
{
    private static final String PROPERTY_START_TLS = "pulse.ldap.use.starttls";

    private static final String COMMON_NAME = "cn";

    private static final Logger LOG = Logger.getLogger(AcegiLdapManager.class);

    protected static final String EMAIL_CONTACT_NAME = "LDAP email";

    /**
     * The configuration settings for the managed ldap connection
     */
    private LDAPConfiguration configuration;
    private LdapAuthenticationProvider authenticationProvider;

    private ConfigurationProvider configurationProvider;
    private UserManager userManager;

    private boolean enabled;
    private boolean initialised;

    private String statusMessage;
    /**
     * A cache of LDAP groups, set on each authenticate or when the user is needed and has not yet authenticated.
     */
    private final Map<String, Set<GrantedAuthority>> groupAuthorities = new HashMap<String, Set<GrantedAuthority>>();

    public synchronized void init(LDAPConfiguration configuration)
    {
        this.initialised = false;
        this.enabled = false;
        this.statusMessage = null;

        this.configuration = configuration;

        // Clear the cache group roles, in case group integration settings have changed.
        this.groupAuthorities.clear();

        if (configuration != null && configuration.isEnabled())
        {
            try
            {
                this.authenticationProvider = getAuthenticationProvider(this.configuration);
                this.enabled = true;
            }
            catch (Exception e)
            {
                LOG.warning(e);
                this.statusMessage = e.getMessage();
            }
        }

        this.initialised = true;
    }

    public synchronized UserConfiguration authenticate(String username, String password, boolean addContact)
    {
        if (!enabled || !initialised)
        {
            return null;
        }

        try
        {
            Authentication authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            DirContextOperations context = getUserSearch(configuration).searchForUser(username);
            String name = getStringAttribute(context, COMMON_NAME, username);
            if (name == null)
            {
                name = username;
            }

            // ldap user may not exist in our system
            UserConfiguration user = userManager.getUserConfig(username);
            if (user == null)
            {
                user = new UserConfiguration(username, name);
                user.setAuthenticatedViaLdap(true);
            }

            if (addContact)
            {
                addEmailContactIfAvailable(user, context);
            }

            groupAuthorities.put(username, new HashSet<GrantedAuthority>(authentication.getAuthorities()));

            return user;
        }
        catch (BadCredentialsException e)
        {
            LOG.info("LDAP login failure: user: " + username + " : " + e.getMessage(), e);
            return null;
        }
        catch (Exception e)
        {
            LOG.warning(e);
            statusMessage = e.getMessage();
            return null;
        }
    }

    /**
     * Read the details, extracting the email address and adding it as a
     * contact point to the user if possible.
     *
     * @param user      the user to which the contact point will be added.
     * @param context   the context providing access to the ldap details.
     */
    private void addEmailContactIfAvailable(UserConfiguration user, DirContextOperations context)
    {
        if (StringUtils.stringSet(configuration.getEmailAttribute()))
        {
            UserPreferencesConfiguration prefs = user.getPreferences();
            Map<String, ContactConfiguration> contacts = prefs.getContacts();
            if (!contacts.containsKey(EMAIL_CONTACT_NAME))
            {
                String email = getStringAttribute(context, configuration.getEmailAttribute(), user.getLogin());
                if (email != null)
                {
                    try
                    {
                        new InternetAddress(email);
                        EmailContactConfiguration contact = new EmailContactConfiguration();
                        contact.setName(EMAIL_CONTACT_NAME);
                        contact.setAddress(email);
                        contact.setPrimary(contacts.isEmpty());
                        contacts.put(EMAIL_CONTACT_NAME, contact);
                    }
                    catch (AddressException e)
                    {
                        LOG.debug("Ignoring invalid email address '" + email + "' for user '" + user.getLogin() + "'");
                    }
                }
            }
        }
    }

    private String getStringAttribute(DirContextOperations details, String attribute, String username)
    {
        String value = details.getStringAttribute(attribute);
        if (value == null)
        {
            LOG.debug("User '" + username + "' has no '" + attribute + "' attribute");
        }
        return value;
    }

    /**
     * Update the user instance, adding to it the groups that are
     * defined within LDAP.
     * <p/>
     * If the user has not been authenticated via LDAP, no changes
     * are made.
     *
     * @param user the user instance to be updated.
     */
    // this helps to track the group associations defined within LDAP.
    public synchronized void addLdapRoles(Principle user)
    {
        String username = user.getUsername();
        if (!groupAuthorities.containsKey(username) && user.getLdapAuthentication())
        {
            // User is defined in LDAP, but has not authenticated since Pulse was started.  If group integration is
            // enabled we have to do a manual bind and group lookup here.
            LDAPConfiguration configuration = configurationProvider.get(LDAPConfiguration.class);
            List<String> groupBaseDns = configuration.getGroupBaseDns();
            if (groupBaseDns != null && !groupBaseDns.isEmpty())
            {
                populateGroupAuthorities(user, username, configuration);
            }
        }

        Set<GrantedAuthority> authorities = groupAuthorities.get(username);
        if (authorities != null)
        {
            for (UserGroupConfiguration group : getLdapGroups(authorities))
            {
                LOG.debug("Adding user '" + username + "' to group '" + group.getName() + "' via LDAP");
                user.addGroup(group);
            }
        }
    }

    private void populateGroupAuthorities(Principle user, String username, LDAPConfiguration configuration)
    {
        try
        {
            DefaultSpringSecurityContextSource context = createContextSource(configuration);
            FilterBasedLdapUserSearch userSearch = createUserSearch(configuration, context);
            DirContextOperations userContext = userSearch.searchForUser(user.getUsername());
            List<DefaultLdapAuthoritiesPopulator> populators = createPopulators(configuration, context);
            Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
            for (DefaultLdapAuthoritiesPopulator populator: populators)
            {
                authorities.addAll(populator.getGroupMembershipRoles(userContext.getNameInNamespace(), username));
            }

            groupAuthorities.put(username, authorities);
        }
        catch (Exception e)
        {
            LOG.severe("Could not determine group membership for user '" + user.getUsername() + "': " + e.getMessage(), e);
        }
    }

    public synchronized boolean canAutoAdd()
    {
        return enabled && initialised && configuration.getAutoAddUsers()  && LicenseHolder.hasAuthorization(AUTH_ADD_USER);
    }

    public synchronized String getStatusMessage()
    {
        return statusMessage;
    }

    public List<UserGroupConfiguration> testAuthenticate(LDAPConfiguration configuration, String username, String password)
    {
        try
        {
            LdapAuthenticationProvider authenticationProvider = getAuthenticationProvider(configuration);
            Authentication authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return getLdapGroups(authentication.getAuthorities());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private List<UserGroupConfiguration> getLdapGroups(Collection<? extends GrantedAuthority> ldapAuthorities)
    {
        List<UserGroupConfiguration> groups = new LinkedList<UserGroupConfiguration>();
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

    protected LdapUserSearch getUserSearch(LDAPConfiguration configuration) throws Exception
    {
        DefaultSpringSecurityContextSource context = createContextSource(configuration);

        return createUserSearch(configuration, context);
    }

    protected LdapAuthenticationProvider getAuthenticationProvider(LDAPConfiguration configuration) throws Exception
    {
        DefaultSpringSecurityContextSource context = createContextSource(configuration);

        BindAuthenticator authenticator = new BindAuthenticator(context);
        authenticator.setUserSearch(createUserSearch(configuration, context));

        LdapAuthoritiesPopulator populator = new NullAuthoritiesPopulator();
        if (configuration.getGroupBaseDns() != null && !configuration.getGroupBaseDns().isEmpty())
        {
            populator = new LdapAuthoritiesPopulators(createPopulators(configuration, context));
        }

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator, populator);

        if (StringUtils.stringSet(configuration.getPasswordAttribute()))
        {
            LdapUserDetailsMapper mapper = new LdapUserDetailsMapper();
            mapper.setPasswordAttributeName(configuration.getPasswordAttribute());
            provider.setUserDetailsContextMapper(mapper);
        }

        return provider;
    }

    private FilterBasedLdapUserSearch createUserSearch(LDAPConfiguration configuration, DefaultSpringSecurityContextSource context)
    {
        String userFilter = configuration.getUserFilter().replace("${login}", "{0}");
        return new FilterBasedLdapUserSearch(configuration.getUserBaseDn(), userFilter, context);
    }

    private DefaultSpringSecurityContextSource createContextSource(LDAPConfiguration configuration) throws Exception
    {
        String providerUrl = configuration.getLdapUrl();
        if (!providerUrl.endsWith("/"))
        {
            providerUrl += '/';
        }

        DefaultSpringSecurityContextSource context = new DefaultSpringSecurityContextSource(providerUrl);
        String baseDn = configuration.getBaseDn();
        String managerDn = configuration.getManagerDn();

        if (configuration.getEscapeSpaceCharacters())
        {
            baseDn = escapeSpaces(baseDn);
            managerDn = escapeSpaces(managerDn);
        }

        context.setBase(baseDn);

        if (StringUtils.stringSet(managerDn))
        {
            context.setAuthenticationSource(new SimpleAuthenticationSource(managerDn, configuration.getManagerPassword()));
        }

        if (configuration.getFollowReferrals())
        {
            context.setReferral("follow");
        }

        if (Boolean.getBoolean(PROPERTY_START_TLS))
        {
            DirContextAuthenticationStrategy authenticationStrategy = new DefaultTlsDirContextAuthenticationStrategy();
            context.setAuthenticationStrategy(authenticationStrategy);
        }

        // this is what happens when we manually set up spring objects.
        context.afterPropertiesSet();
        return context;
    }

    private List<DefaultLdapAuthoritiesPopulator> createPopulators(LDAPConfiguration configuration, ContextSource context)
    {
        List<DefaultLdapAuthoritiesPopulator> populators = new LinkedList<DefaultLdapAuthoritiesPopulator>();
        for (String groupDn : configuration.getGroupBaseDns())
        {
            if (configuration.getEscapeSpaceCharacters())
            {
                groupDn = escapeSpaces(groupDn);
            }

            DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(context, groupDn);
            if (StringUtils.stringSet(configuration.getGroupSearchFilter()))
            {
                populator.setGroupSearchFilter(convertGroupFilter(configuration.getGroupSearchFilter()));
            }

            if (StringUtils.stringSet(configuration.getGroupRoleAttribute()))
            {
                populator.setGroupRoleAttribute(configuration.getGroupRoleAttribute());
            }

            populator.setSearchSubtree(configuration.getSearchGroupSubtree());
            populator.setRolePrefix("");
            populator.setConvertToUpperCase(false);
            populators.add(populator);
        }
        return populators;
    }

    private String escapeSpaces(String dn)
    {
        return dn.replaceAll(" ", "\\\\20");
    }

    private String convertGroupFilter(String groupFilter)
    {
        return groupFilter.replace("${user.dn}", "{0}").replace("${login}", "{1}");
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
            configurationProvider = ((ConfigurationEventSystemStartedEvent)event).getConfigurationProvider();
            configurationProvider.registerEventListener(this, false, false, LDAPConfiguration.class);
        }
        else
        {
            init(configurationProvider.get(LDAPConfiguration.class));
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationEventSystemStartedEvent.class, ConfigurationSystemStartedEvent.class };
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    private class SimpleAuthenticationSource implements AuthenticationSource
    {
        private String principal;
        private String credentials;

        private SimpleAuthenticationSource(String principal, String credentials)
        {
            this.principal = principal;
            this.credentials = credentials;
        }

        public String getPrincipal()
        {
            return principal;
        }

        public String getCredentials()
        {
            return credentials;
        }
    }

    private class NullAuthoritiesPopulator implements LdapAuthoritiesPopulator
    {
        public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userDetails, String username)
        {
            return new LinkedList<GrantedAuthority>();
        }
    }

    private class LdapAuthoritiesPopulators implements LdapAuthoritiesPopulator
    {
        private List<? extends LdapAuthoritiesPopulator> populators;

        private LdapAuthoritiesPopulators(List<? extends LdapAuthoritiesPopulator> populators)
        {
            this.populators = populators;
        }

        public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username)
        {
            Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();

            for (LdapAuthoritiesPopulator populator : populators)
            {
                grantedAuthorities.addAll(populator.getGrantedAuthorities(userData, username));
            }
            return grantedAuthorities;
        }
    }
}
