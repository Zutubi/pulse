package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.annotation.Form;

/**
 *
 *
 */
@Form(fieldOrder={"baseUrl", "baseHelpUrl", "recipeTimeout", "scmPollingInterval"})
public class GeneralAdminConfiguration
{
    private String baseUrl;
    private String baseHelpUrl;
    private boolean rssEnabled;
    private boolean anonymousAccessEnabled;
    private boolean anonymousSignupEnabled;
    private int scmPollingInterval;
    private boolean recipeTimeoutEnabled;
    private int recipeTimeout;

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
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
