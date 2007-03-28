package com.zutubi.pulse.security.ldap;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.util.logging.Logger;
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
public class AcegiLdapManager implements LdapManager
{
    private static final Logger LOG = Logger.getLogger(AcegiLdapManager.class);

    private static final String EMAIL_CONTACT_NAME = "LDAP email";

    private boolean initialised = false;
    private MasterConfigurationManager configurationManager;
    private boolean enabled = false;
    private DefaultInitialDirContextFactory contextFactory;
    private BindAuthenticator authenticator;
    private DefaultLdapAuthoritiesPopulator populator = null;
    private boolean autoAdd = false;
    private String statusMessage = null;
    private String emailAttribute = null;
    private UserManager userManager;
    private Map<String, LdapUserDetails> detailsMap = new HashMap<String, LdapUserDetails>();

    public void init()
    {
        initialised = false;
        statusMessage = null;
        MasterConfiguration appConfig = configurationManager.getAppConfig();
        enabled = appConfig.getLdapEnabled();
        autoAdd = appConfig.getLdapAutoAdd();
        emailAttribute = appConfig.getLdapEmailAttribute();

        if (enabled)
        {
            String hostUrl = appConfig.getLdapHostUrl();
            String baseDn = appConfig.getLdapBaseDn();
            String managerDn = appConfig.getLdapManagerDn();
            String managerPassword = appConfig.getLdapManagerPassword();
            boolean followReferrals = appConfig.getLdapFollowReferrals();
            boolean escapeSpaces = appConfig.getLdapEscapeSpaces();

            contextFactory = createContextFactory(hostUrl, baseDn, managerDn, managerPassword, followReferrals, escapeSpaces);
            authenticator = createAuthenticator(appConfig.getLdapUserBase(), appConfig.getLdapUserFilter(), contextFactory);

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

            if (TextUtils.stringSet(appConfig.getLdapGroupBaseDn()))
            {
                populator = createPopulator(appConfig.getLdapGroupBaseDn(), appConfig.getLdapGroupFilter(), appConfig.getLdapGroupRoleAttribute(), appConfig.getLdapGroupSearchSubtree(), escapeSpaces, contextFactory);
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

    public void connect()
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

    public User authenticate(String username, String password)
    {
        if (enabled && initialised)
        {
            try
            {
                LdapUserDetails details = authenticator.authenticate(username, password);
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

    public void addLdapRoles(AcegiUser user)
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
            LOG.debug("User '" + username + "' has no common name (cn) attribute");
        }

        return null;
    }

    public boolean canAutoAdd()
    {
        return enabled && initialised && autoAdd && LicenseHolder.hasAuthorization("canAddUser");
    }

    public List<Group> testAuthenticate(String hostUrl, String baseDn, String managerDn, String managerPassword,
                                        String userBase, String userFilter,
                                        String groupDn, String groupFilter, String groupRoleAttribute, boolean groupSearchSubtree,
                                        boolean followReferrals, boolean escapeSpaces,
                                        String testLogin, String testPassword)
    {
        DefaultInitialDirContextFactory contextFactory = createContextFactory(hostUrl, baseDn, managerDn, managerPassword, followReferrals, escapeSpaces);
        contextFactory.newInitialDirContext();

        BindAuthenticator authenticator = createAuthenticator(userBase, userFilter, contextFactory);
        LdapUserDetails details = authenticator.authenticate(testLogin, testPassword);

        if(TextUtils.stringSet(groupDn))
        {
            DefaultLdapAuthoritiesPopulator populator = createPopulator(groupDn, groupFilter, groupRoleAttribute, groupSearchSubtree, escapeSpaces, contextFactory);
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

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
