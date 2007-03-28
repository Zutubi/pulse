package com.zutubi.pulse.web.admin;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 * Action to configure LDAP properties for authentication via an LDAP
 * server.
 */
public class LdapConfigurationAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;

    private LdapConfigurationAction.LdapConfig ldap = new LdapConfigurationAction.LdapConfig();
    private LdapManager ldapManager;

    public LdapConfigurationAction.LdapConfig getLdap()
    {
        return ldap;
    }

    public String doReset()
    {
        resetConfig();
        loadConfig();
        return SUCCESS;
    }

    public String doSave()
    {
        saveConfig();

        return SUCCESS;
    }

    public String doInput()
    {
        loadConfig();

        return INPUT;
    }

    public void validate()
    {
        if(ldap.getEnabled())
        {
            if(!TextUtils.stringSet(ldap.getHost()))
            {
                addFieldError("ldap.host", getText("ldap.host.url.required"));
            }

            if(!TextUtils.stringSet(ldap.getBaseDn()))
            {
                addFieldError("ldap.baseDn", getText("ldap.base.dn.required"));
            }

            if(!TextUtils.stringSet(ldap.getUserFilter()))
            {
                addFieldError("ldap.userFilter", getText("ldap.user.filter.required"));
            }
        }
    }

    public String execute()
    {
        // default action, load the config details.
        loadConfig();

        return SUCCESS;
    }

    private void resetConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        config.setLdapEnabled(false);
        config.setLdapHostUrl("");
        config.setLdapBaseDn("");
        config.setLdapManagerDn("");
        config.setLdapManagerPassword("");
        config.setLdapUserBase("");
        config.setLdapUserFilter("");
        config.setLdapEmailAttribute("");
        config.setLdapAutoAdd(false);
        config.setLdapGroupBaseDn("");
        config.setLdapGroupFilter("");
        config.setLdapGroupRoleAttribute("");
        config.setLdapGroupSearchSubtree(false);
        config.setLdapFollowReferrals(false);
        config.setLdapEscapeSpaces(false);
        ldapManager.init();
    }

    private void saveConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        config.setLdapEnabled(ldap.getEnabled());
        config.setLdapHostUrl(ldap.getHost());
        config.setLdapBaseDn(ldap.getBaseDn());
        config.setLdapManagerDn(ldap.getManagerDn());
        config.setLdapManagerPassword(ldap.getManagerPassword());
        config.setLdapUserBase(ldap.getUserBase());
        config.setLdapUserFilter(ldap.getUserFilter());
        config.setLdapAutoAdd(ldap.getAutoAdd());
        config.setLdapEmailAttribute(ldap.getEmailAttribute());
        config.setLdapGroupBaseDn(ldap.getGroupBaseDn());
        config.setLdapGroupFilter(ldap.getGroupFilter());
        config.setLdapGroupRoleAttribute(ldap.getGroupRoleAttribute());
        config.setLdapGroupSearchSubtree(ldap.getGroupSearchSubtree());
        config.setLdapFollowReferrals(ldap.getFollowReferrals());
        config.setLdapEscapeSpaces(ldap.getEscapeSpaces());
        ldapManager.init();
        if(ldap.getEnabled())
        {
            ldapManager.connect();
        }

    }

    private void loadConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        ldap.setEnabled(config.getLdapEnabled());
        ldap.setHost(config.getLdapHostUrl());
        ldap.setBaseDn(config.getLdapBaseDn());
        ldap.setManagerDn(config.getLdapManagerDn());
        ldap.setManagerPassword(config.getLdapManagerPassword());
        ldap.setUserBase(config.getLdapUserBase());
        ldap.setUserFilter(config.getLdapUserFilter());
        ldap.setAutoAdd(config.getLdapAutoAdd());
        ldap.setEmailAttribute(config.getLdapEmailAttribute());
        ldap.setGroupBaseDn(config.getLdapGroupBaseDn());
        ldap.setGroupFilter(config.getLdapGroupFilter());
        ldap.setGroupRoleAttribute(config.getLdapGroupRoleAttribute());
        ldap.setGroupSearchSubtree(config.getLdapGroupSearchSubtree());
        ldap.setFollowReferrals(config.getLdapFollowReferrals());
        ldap.setEscapeSpaces(config.getLdapEscapeSpaces());
    }

    /**
     * Required resource, provides access to the Jabber configuration details.
     *
     * @param config
     */
    public void setConfigurationManager(MasterConfigurationManager config)
    {
        this.configurationManager = config;
    }

    public void setLdapManager(LdapManager ldapManager)
    {
        this.ldapManager = ldapManager;
    }

    /**
     * Holder for the form post.
     */
    public class LdapConfig
    {
        private Boolean enabled = false;
        private String host;
        private String baseDn;
        private String managerDn;
        private String managerPassword;
        private String userBase;
        private String userFilter;
        private Boolean autoAdd = false;
        private String emailAttribute;
        private String groupBaseDn;
        private String groupFilter;
        private String groupRoleAttribute;
        private Boolean groupSearchSubtree = false;
        private Boolean followReferrals = false;
        private Boolean escapeSpaces = false;

        public Boolean getEnabled()
        {
            return enabled;
        }

        public void setEnabled(Boolean enabled)
        {
            this.enabled = enabled;
        }

        public String getHost()
        {
            return host;
        }

        public void setHost(String host)
        {
            this.host = host;
        }

        public String getBaseDn()
        {
            return baseDn;
        }

        public void setBaseDn(String baseDn)
        {
            this.baseDn = baseDn;
        }

        public String getManagerDn()
        {
            return managerDn;
        }

        public void setManagerDn(String managerDn)
        {
            this.managerDn = managerDn;
        }

        public String getManagerPassword()
        {
            return managerPassword;
        }

        public void setManagerPassword(String managerPassword)
        {
            this.managerPassword = managerPassword;
        }

        public String getUserBase()
        {
            return userBase;
        }

        public void setUserBase(String userBase)
        {
            this.userBase = userBase;
        }

        public String getUserFilter()
        {
            return userFilter;
        }

        public void setUserFilter(String userFilter)
        {
            this.userFilter = userFilter;
        }

        public Boolean getAutoAdd()
        {
            return autoAdd;
        }

        public void setAutoAdd(Boolean autoAdd)
        {
            this.autoAdd = autoAdd;
        }

        public String getEmailAttribute()
        {
            return emailAttribute;
        }

        public void setEmailAttribute(String emailAttribute)
        {
            this.emailAttribute = emailAttribute;
        }

        public String getGroupBaseDn()
        {
            return groupBaseDn;
        }

        public void setGroupBaseDn(String groupBaseDn)
        {
            this.groupBaseDn = groupBaseDn;
        }

        public String getGroupFilter()
        {
            return groupFilter;
        }

        public void setGroupFilter(String groupFilter)
        {
            this.groupFilter = groupFilter;
        }

        public String getGroupRoleAttribute()
        {
            return groupRoleAttribute;
        }

        public void setGroupRoleAttribute(String groupRoleAttribute)
        {
            this.groupRoleAttribute = groupRoleAttribute;
        }

        public Boolean getGroupSearchSubtree()
        {
            return groupSearchSubtree;
        }

        public void setGroupSearchSubtree(Boolean groupSearchSubtree)
        {
            this.groupSearchSubtree = groupSearchSubtree;
        }

        public Boolean getFollowReferrals()
        {
            return followReferrals;
        }

        public void setFollowReferrals(Boolean followReferrals)
        {
            this.followReferrals = followReferrals;
        }

        public Boolean getEscapeSpaces()
        {
            return escapeSpaces;
        }

        public void setEscapeSpaces(Boolean escapeSpaces)
        {
            this.escapeSpaces = escapeSpaces;
        }
    }
}
