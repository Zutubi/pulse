package com.zutubi.pulse.security.ldap;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterApplicationConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.license.LicenseHolder;
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

    public void init()
    {
        initialised = false;
        statusMessage = null;
        MasterApplicationConfiguration appConfig = configurationManager.getAppConfig();
        enabled = appConfig.getLdapEnabled();
        autoAdd = appConfig.getLdapAutoAdd();

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

    private String convertFilter(MasterApplicationConfiguration appConfig)
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
            LOG.error("Unable to connect to LDAP server: " + e.getMessage(), e);
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
                String name = username;
                Attribute commonName = details.getAttributes().get("cn");
                if(commonName != null)
                {
                    try
                    {
                        Object value = commonName.get();
                        if(value instanceof String)
                        {
                            name = (String) value;
                        }
                    }
                    catch (NamingException e)
                    {
                        LOG.debug("Unable to get common name for user '" + username + "': " + e.getMessage(),  e);
                    }
                }
                else
                {
                    LOG.debug("User '" + username + "' has no common name (cn) attribute");
                }

                return new User(username, name);
            }
            catch(BadCredentialsException e)
            {
                LOG.info("LDAP login failure: user: " + username + " : " + e.getMessage(), e);
            }
            catch(Exception e)
            {
                LOG.warning("Error contacting LDAP server: " + e.getMessage(), e);
                statusMessage = e.getMessage();
            }
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
