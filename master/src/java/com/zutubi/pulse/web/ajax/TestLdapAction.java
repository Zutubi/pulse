package com.zutubi.pulse.web.ajax;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 * An ajax request to test LDAP settings and send a fragment of HTML
 * with results.
 */
public class TestLdapAction extends ActionSupport
{
    private LdapManager ldapManager;

    private String host;
    private String baseDn;
    private String managerDn;
    private String managerPassword;

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setBaseDn(String baseDn)
    {
        this.baseDn = baseDn;
    }

    public void setManagerDn(String managerDn)
    {
        this.managerDn = managerDn;
    }

    public void setManagerPassword(String managerPassword)
    {
        this.managerPassword = managerPassword;
    }

    public String execute() throws Exception
    {
        if(!TextUtils.stringSet(host))
        {
            addActionError(getText("ldap.host.url.required"));
        }

        if(!TextUtils.stringSet(baseDn))
        {
            addActionError(getText("ldap.base.dn.required"));
        }

        if (!hasErrors())
        {
            try
            {
                ldapManager.test(host, baseDn, managerDn, managerPassword);
            }
            catch(Exception e)
            {
                addActionError(e.getMessage());
            }
        }

        return SUCCESS;
    }

    public void setLdapManager(LdapManager ldapManager)
    {
        this.ldapManager = ldapManager;
    }
}
