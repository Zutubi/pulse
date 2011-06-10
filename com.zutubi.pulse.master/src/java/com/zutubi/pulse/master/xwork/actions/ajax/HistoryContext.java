package com.zutubi.pulse.master.xwork.actions.ajax;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * Manages storage and retrieval of history page related preferences.
 */
public class HistoryContext
{
    public static final String SESSION_KEY_BUILDS_PER_PAGE = "pulse.historyBuildsPerPage";

    public static int getBuildsPerPage(User user)
    {
        int buildsPerPage = UserPreferencesConfiguration.DEFAULT_HISTORY_BUILDS_PER_PAGE;
        if (user == null)
        {
            Integer sessionValue = (Integer) ActionContext.getContext().getSession().get(HistoryContext.SESSION_KEY_BUILDS_PER_PAGE);
            if (sessionValue != null)
            {
                buildsPerPage = sessionValue;
            }
        }
        else
        {
            buildsPerPage = user.getPreferences().getHistoryBuildsPerPage();
        }
    
        if (buildsPerPage <= 0)
        {
            buildsPerPage = UserPreferencesConfiguration.DEFAULT_HISTORY_BUILDS_PER_PAGE;
        }
    
        return buildsPerPage;
    }
    
    @SuppressWarnings({"unchecked"})
    public static void setBuildsPerPage(User user, int buildsPerPage, ConfigurationProvider configurationProvider)
    {
        if (user == null)
        {
            // Store in the session only.
            ActionContext.getContext().getSession().put(HistoryContext.SESSION_KEY_BUILDS_PER_PAGE, buildsPerPage);
        }
        else
        {
            // Save forever.
            UserPreferencesConfiguration preferences = configurationProvider.deepClone(user.getPreferences());
            preferences.setHistoryBuildsPerPage(buildsPerPage);
            configurationProvider.save(preferences);
        }
    }
}
