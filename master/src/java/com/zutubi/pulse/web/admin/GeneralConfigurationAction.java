package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.GuestAccessManager;
import com.zutubi.pulse.ThreadedRecipeQueue;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.web.ActionSupport;

/**
 * <class-comment/>
 */
public class GeneralConfigurationAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;
    private GuestAccessManager guestAccessManager;

    private String baseUrl;
    private String helpUrl;
    private boolean rssEnabled;
    private boolean anonEnabled;
    private Integer scmPollingInterval;
    private boolean recipeTimeoutEnabled;
    private Long recipeTimeout;
    private ThreadedRecipeQueue recipeQueue;

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

    public String execute()
    {
        // default action, load the config details.
        loadConfig();

        return SUCCESS;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getHelpUrl()
    {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl)
    {
        this.helpUrl = helpUrl;
    }

    public boolean isRssEnabled()
    {
        return rssEnabled;
    }

    public void setRssEnabled(boolean rssEnabled)
    {
        this.rssEnabled = rssEnabled;
    }

    public boolean isAnonEnabled()
    {
        return anonEnabled;
    }

    public void setAnonEnabled(boolean anonEnabled)
    {
        this.anonEnabled = anonEnabled;
    }

    public Integer getScmPollingInterval()
    {
        return scmPollingInterval;
    }

    public void setScmPollingInterval(Integer scmPollingInterval)
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

    public Long getRecipeTimeout()
    {
        return recipeTimeout;
    }

    public void setRecipeTimeout(Long recipeTimeout)
    {
        this.recipeTimeout = recipeTimeout;
    }

    private void resetConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        config.setBaseUrl(null);
        config.setHelpUrl(null);
        config.setRssEnabled(null);
        config.setAnonymousAccessEnabled(null);
        config.setScmPollingInterval(null);
        config.setUnsatisfiableRecipeTimeout(null);

        postChange(config);
    }

    private void saveConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        config.setBaseUrl(baseUrl);
        config.setHelpUrl(helpUrl);
        config.setRssEnabled(rssEnabled);
        config.setAnonymousAccessEnabled(anonEnabled);
        config.setScmPollingInterval(scmPollingInterval);

        if(recipeTimeoutEnabled)
        {
            config.setUnsatisfiableRecipeTimeout(recipeTimeout);
        }
        else
        {
            config.setUnsatisfiableRecipeTimeout((long) -1);
        }

        postChange(config);
    }

    private void postChange(MasterConfiguration config)
    {
        long timeout = config.getUnsatisfiableRecipeTimeout();
        if(timeout > 0)
        {
            timeout *= Constants.MINUTE;
        }
        recipeQueue.setUnsatisfiableTimeout(timeout);
        guestAccessManager.init();
    }

    private void loadConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        baseUrl = config.getBaseUrl();
        helpUrl = config.getHelpUrl();
        rssEnabled = config.getRssEnabled();
        anonEnabled = config.getAnonymousAccessEnabled();
        scmPollingInterval = config.getScmPollingInterval();

        long timeout = config.getUnsatisfiableRecipeTimeout();
        if(timeout >= 0)
        {
            recipeTimeout = timeout;
            recipeTimeoutEnabled = true;
        }
        else
        {
            recipeTimeout = MasterConfiguration.UNSATISFIABLE_RECIPE_TIMEOUT_DEFAULT;
            recipeTimeoutEnabled = false;
        }
    }

    public void validate()
    {
        if(recipeTimeoutEnabled)
        {
            if(recipeTimeout == null)
            {
                addFieldError("recipeTimeout", getText("recipe.queue.timeout.required"));
            }
            else
            {
                if (recipeTimeout < 0)
                {
                    addFieldError("recipeTimeout", getText("recipe.queue.timeout.invalid"));
                }
            }
        }
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setGuestAccessManager(GuestAccessManager guestAccessManager)
    {
        this.guestAccessManager = guestAccessManager;
    }

    public void setRecipeQueue(ThreadedRecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }
}
