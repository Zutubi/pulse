package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * Action to save tail view settings to a user's preferences.  Invalid settings
 * are ignored.
 */
public class SaveTailSettingsAction extends ActionSupport
{
    private int maxLines = -1;
    private int refreshInterval = -1;
    private SimpleResult result;

    private ConfigurationProvider configurationProvider;

    public void setMaxLines(int maxLines)
    {
        this.maxLines = maxLines;
    }

    public void setRefreshInterval(int refreshInterval)
    {
        this.refreshInterval = refreshInterval;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    @Override
    public String execute() throws Exception
    {
        Object principle = getPrinciple();
        if (principle != null)
        {
            User user = userManager.getUser((String) principle);
            if (user != null)
            {
                boolean changed = false;

                UserPreferencesConfiguration preferences = user.getPreferences();
                if (refreshInterval <= 0)
                {
                    refreshInterval = preferences.getTailRefreshInterval();
                }
                else if (refreshInterval != preferences.getTailRefreshInterval())
                {
                    preferences = configurationProvider.deepClone(preferences);
                    preferences.setTailRefreshInterval(refreshInterval);
                    changed = true;
                }

                if (maxLines <= 0)
                {
                    maxLines = preferences.getTailLines();
                }
                else if (maxLines != preferences.getTailLines())
                {
                    if (!changed)
                    {
                        preferences = configurationProvider.deepClone(preferences);
                    }
                    preferences.setTailLines(maxLines);
                    changed = true;
                }

                if (changed)
                {
                    configurationProvider.save(preferences);
                }
            }
        }

        result = new SimpleResult(true, "settings saved");
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
