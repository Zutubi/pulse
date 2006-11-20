package com.zutubi.pulse.web.ajax;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.pulse.web.ActionSupport;

import java.util.List;

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
    private String userFilter;
    private String groupDn;
    private String groupFilter;
    private String groupRoleAttribute;
    private boolean groupSearchSubtree;
    private boolean escapeSpaces;
    private String login;
    private String password;
    private List<Group> groups;

    public List<Group> getGroups()
    {
        return groups;
    }

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

    public void setEscapeSpaces(boolean escapeSpaces)
    {
        this.escapeSpaces = escapeSpaces;
    }

    public void setUserFilter(String userFilter)
    {
        this.userFilter = userFilter;
    }

    public void setGroupDn(String groupDn)
    {
        this.groupDn = groupDn;
    }

    public void setGroupFilter(String groupFilter)
    {
        this.groupFilter = groupFilter;
    }

    public void setGroupRoleAttribute(String groupRoleAttribute)
    {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public void setGroupSearchSubtree(boolean groupSearchSubtree)
    {
        this.groupSearchSubtree = groupSearchSubtree;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public void setPassword(String password)
    {
        this.password = password;
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

        if(!TextUtils.stringSet(login))
        {
            addActionError(getText("ldap.test.login.required"));
        }

        if (!hasErrors())
        {
            try
            {
                groups = ldapManager.testAuthenticate(host, baseDn, managerDn, managerPassword, userFilter, groupDn, groupFilter, groupRoleAttribute, groupSearchSubtree, escapeSpaces, login, password);
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
