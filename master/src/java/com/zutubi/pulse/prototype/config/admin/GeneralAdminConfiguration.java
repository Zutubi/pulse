package com.zutubi.pulse.prototype.config.admin;

import com.opensymphony.util.TextUtils;
import com.zutubi.config.annotations.Form;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
@SymbolicName("generalConfig")
@Form(fieldOrder={"baseUrl", "masterHost", "baseHelpUrl", "rssEnabled", "anonymousAccessEnabled", "anonymousSignupEnabled", "scmPollingInterval", "recipeTimeoutEnabled", "recipeTimeout" })
public class GeneralAdminConfiguration
{
    private String baseUrl;
    private String masterHost;
    private String baseHelpUrl = "http://confluence.zutubi.com/display/pulse0200";
    private boolean rssEnabled = true;
    private boolean anonymousAccessEnabled = false;
    private boolean anonymousSignupEnabled = false;
    private int scmPollingInterval = 5;
    private boolean recipeTimeoutEnabled = true;
    private int recipeTimeout = 15;

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        // munge the url a little. We assume that there is no trailing '/' when using this property.
        if (TextUtils.stringSet(baseUrl) && baseUrl.endsWith("/"))
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

    public void setRecipeTimeout(int recipeTimeout)
    {
        this.recipeTimeout = recipeTimeout;
    }
}
