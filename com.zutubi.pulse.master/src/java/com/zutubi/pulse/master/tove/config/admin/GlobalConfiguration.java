/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.notifications.jabber.config.JabberConfiguration;
import com.zutubi.pulse.master.restore.BackupConfiguration;
import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * The global configuration scope, which holds server-administration configuration.
 */
@SymbolicName("zutubi.globalConfig")
@Classification(single = "settings")
@Form(fieldOrder={"baseUrl", "masterHost", "baseHelpUrl", "rssEnabled", "anonymousAccessEnabled", "anonymousSignupEnabled", "scmPollingInterval", "recipeTimeoutEnabled", "recipeTimeout" })
public class GlobalConfiguration extends AbstractConfiguration
{
    public static final String SCOPE_NAME = "settings";

    private String baseUrl;
    private String masterHost;
    private String baseHelpUrl = "http://confluence.zutubi.com/display/pulse0300";
    private boolean rssEnabled = true;
    private boolean anonymousAccessEnabled = false;
    private boolean anonymousSignupEnabled = false;
    private int scmPollingInterval = 5;

    @ControllingCheckbox(checkedFields = {"recipeTimeout"})
    private boolean recipeTimeoutEnabled = true;
    private int recipeTimeout = 15;

    private LoggingConfiguration logging = new LoggingConfiguration();
    private EmailConfiguration email = new EmailConfiguration();
    private LDAPConfiguration ldap = new LDAPConfiguration();
    private JabberConfiguration jabber = new JabberConfiguration();
    private BackupConfiguration backup = new BackupConfiguration();
    private RepositoryConfiguration repository = new RepositoryConfiguration();
    private AgentPingConfiguration agentPing = new AgentPingConfiguration();
    private Map<String, ResourceConfiguration> resources = new HashMap<>();


    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        // munge the url a little. We assume that there is no trailing '/' when using this property.
        if (StringUtils.stringSet(baseUrl) && baseUrl.endsWith("/"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
    }

    public String getMasterHost()
    {
        return masterHost;
    }

    public void setMasterHost(String masterHost)
    {
        this.masterHost = masterHost;
    }

    public String getBaseHelpUrl()
    {
        return baseHelpUrl;
    }

    public void setBaseHelpUrl(String baseHelpUrl)
    {
        this.baseHelpUrl = baseHelpUrl;
    }

    public boolean isRssEnabled()
    {
        return rssEnabled;
    }

    public void setRssEnabled(boolean rssEnabled)
    {
        this.rssEnabled = rssEnabled;
    }

    public boolean isAnonymousAccessEnabled()
    {
        return anonymousAccessEnabled;
    }

    public void setAnonymousAccessEnabled(boolean anonymousAccessEnabled)
    {
        this.anonymousAccessEnabled = anonymousAccessEnabled;
    }

    public boolean isAnonymousSignupEnabled()
    {
        return anonymousSignupEnabled;
    }

    public void setAnonymousSignupEnabled(boolean anonymousSignupEnabled)
    {
        this.anonymousSignupEnabled = anonymousSignupEnabled;
    }

    public int getScmPollingInterval()
    {
        return scmPollingInterval;
    }

    public void setScmPollingInterval(int scmPollingInterval)
    {
        this.scmPollingInterval = scmPollingInterval;
    }

    public boolean isRecipeTimeoutEnabled()
    {
        return recipeTimeoutEnabled;
    }

    public void setRecipeTimeoutEnabled(boolean recipeTimeoutEnabled)
    {
        this.recipeTimeoutEnabled = recipeTimeoutEnabled;
    }

    public int getRecipeTimeout()
    {
        return recipeTimeout;
    }

    public void setRecipeTimeout(int minutes)
    {
        this.recipeTimeout = minutes;
    }

    public LoggingConfiguration getLogging()
    {
        return logging;
    }

    public void setLogging(LoggingConfiguration logging)
    {
        this.logging = logging;
    }

    public EmailConfiguration getEmail()
    {
        return email;
    }

    public void setEmail(EmailConfiguration email)
    {
        this.email = email;
    }

    public LDAPConfiguration getLdap()
    {
        return ldap;
    }

    public void setLdap(LDAPConfiguration ldap)
    {
        this.ldap = ldap;
    }

    public JabberConfiguration getJabber()
    {
        return jabber;
    }

    public void setJabber(JabberConfiguration jabber)
    {
        this.jabber = jabber;
    }

    public BackupConfiguration getBackup()
    {
        return backup;
    }

    public void setBackup(BackupConfiguration backup)
    {
        this.backup = backup;
    }

    public RepositoryConfiguration getRepository()
    {
        return repository;
    }

    public void setRepository(RepositoryConfiguration repository)
    {
        this.repository = repository;
    }

    public AgentPingConfiguration getAgentPing()
    {
        return agentPing;
    }

    public void setAgentPing(AgentPingConfiguration agentPing)
    {
        this.agentPing = agentPing;
    }

    public Map<String, ResourceConfiguration> getResources()
    {
        return resources;
    }

    public void setResources(Map<String, ResourceConfiguration> resources)
    {
        this.resources = resources;
    }
}
