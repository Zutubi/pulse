package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.security.LastAccessManager;
import com.zutubi.tove.config.api.Classifier;

/**
 * Classifies users.
 */
public class UserConfigurationClassifier implements Classifier<UserConfiguration>
{
    private static final String CLASS_ACTIVE = "active-user";
    private static final String CLASS_NORMAL = "user";

    private LastAccessManager lastAccessManager;

    public String classify(UserConfiguration userConfiguration)
    {
        if (userConfiguration != null && lastAccessManager.isActive(userConfiguration.getUserId(), System.currentTimeMillis()))
        {
            return CLASS_ACTIVE;
        }
        else
        {
            return CLASS_NORMAL;
        }
    }

    public void setLastAccessManager(LastAccessManager lastAccessManager)
    {
        this.lastAccessManager = lastAccessManager;
    }
}
