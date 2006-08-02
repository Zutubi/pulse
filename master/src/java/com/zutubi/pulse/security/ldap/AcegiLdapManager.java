package com.zutubi.pulse.security.ldap;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.util.logging.Logger;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.ldap.DefaultInitialDirContextFactory;
import org.acegisecurity.ldap.search.FilterBasedLdapUserSearch;
import org.acegisecurity.providers.ldap.authenticator.BindAuthenticator;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;

/**
 */
public class AcegiLdapManager implements LdapManager
{
    private static final Logger LOG = Logger.getLogger(AcegiLdapManager.class);

    private boolean initialised = false;

    private MasterConfigurationManager configurationManager;
    private boolean enabled = false;
    private DefaultInitialDirContextFactory contextFactory;
    private BindAuthenticator authenticator;
    private boolean autoAdd = false;
    private String statusMessage = null;
    private String emailAttribute = null;

    public void init()
    {
        initialised = false;
        statusMessage = null;
        MasterConfiguration appConfig = configurationManager.getAppConfig();
        enabled = appConfig.getLdapEnabled();
        autoAdd = appConfig.getLdapAutoAdd();
        emailAttribute = appConfig.getLdapEmailAttribute();

        if(enabled)
        {
            String hostUrl = appConfig.getLdapHostUrl();
            String baseDn = appConfig.getLdapBaseDn();
            String managerDn = appConfig.getLdapManagerDn();
            String managerPassword = appConfig.getLdapManagerPassword();

            contextFactory = createContextFactory(hostUrl, baseDn, managerDn, managerPassword);

            FilterBasedLdapUserSearch search = new FilterBasedLdapUserSearch("", convertFilter(appConfig), contextFactory);
            search.setSearchSubtree(true);

            authenticator = new BindAuthenticator(contextFactory);
            authenticator.setUserSearch(search);

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
        }
        else
        {
            contextFactory = null;
            authenticator = null;
        }

        initialised = true;
    }

    private String convertFilter(MasterConfiguration appConfig)
    {
        return appConfig.getLdapUserFilter().replace("${login}", "{0}");
    }

    public void connect()
    {
        statusMessage = null;
        try
        {
            contextFactory.newInitialDirContext();
        }
        catch(Exception e)
        {
            LOG.warning("Unable to connect to LDAP server: " + e.getMessage());
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
                if(name == null)
                {
                    name = username;
                }

                User user = new User(username, name);

                if(TextUtils.stringSet(emailAttribute))
                {
                    addContact(user, details);
                }

                return user;
            }
            catch(BadCredentialsException e)
            {
                LOG.info("LDAP login failure: user: " + username + " : " + e.getMessage(), e);
            }
            catch(Exception e)
            {
                LOG.warning("Error contacting LDAP server: " + e.getMessage());
                statusMessage = e.getMessage();
            }
        }

        return null;
    }

    private void addContact(User user, LdapUserDetails details)
    {
        String email = getStringAttribute(details, emailAttribute, user.getLogin());
        if(email != null)
        {
            EmailContactPoint point = new EmailContactPoint(email);
            point.setName("LDAP email");
            user.add(point);
        }
    }

    private String getStringAttribute(LdapUserDetails details, String attribute, String username)
    {
        Attribute att = details.getAttributes().get(attribute);
        if(att != null)
        {
            try
            {
                Object value = att.get();
                if(value instanceof String)
                {
                    return (String) value;
                }
            }
            catch (NamingException e)
            {
                LOG.debug("Unable to get attribute '" + attribute + "' for user '" + username + "': " + e.getMessage(),  e);
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

    private DefaultInitialDirContextFactory createContextFactory(String hostUrl, String baseDn, String managerDn, String managerPassword)
    {
        if(!hostUrl.endsWith("/"))
        {
            hostUrl += '/';
        }

        DefaultInitialDirContextFactory result = new DefaultInitialDirContextFactory(hostUrl + baseDn);

        if(TextUtils.stringSet(managerDn))
        {
            result.setManagerDn(managerDn);

            if(TextUtils.stringSet(managerPassword))
            {
                result.setManagerPassword(managerPassword);
            }
        }

        return result;
    }

    public void test(String hostUrl, String baseDn, String managerDn, String managerPassword)
    {
        DefaultInitialDirContextFactory contextFactory = createContextFactory(hostUrl, baseDn, managerDn, managerPassword);
        contextFactory.newInitialDirContext();
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }
}
