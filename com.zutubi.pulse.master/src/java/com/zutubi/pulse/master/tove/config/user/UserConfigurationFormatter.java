package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.security.LastAccessManager;
import com.zutubi.util.Constants;
import com.zutubi.util.time.TimeStamps;

public class UserConfigurationFormatter
{
    private LastAccessManager lastAccessManager;

    public String getActive(UserConfiguration userConfig)
    {
        long now = System.currentTimeMillis();

        String accessElapsed;
        long time = lastAccessManager.getLastAccessTime(userConfig.getUserId());
        if (time != LastAccessManager.ACCESS_NEVER)
        {
            long elapsed = now - time;
            if (elapsed < Constants.SECOND)
            {
                accessElapsed = "< 1 second ago";
            }
            else
            {
                int maxUnits = (elapsed < Constants.MINUTE) ? 1 : 2;
                accessElapsed = TimeStamps.getPrettyElapsed(elapsed, maxUnits) + " ago";
            }
        }
        else
        {
            accessElapsed = "never logged in";
        }

        String prefix = lastAccessManager.isActive(userConfig.getUserId(), now) ? "yes" : "no";
        return prefix + " (" + accessElapsed + ")";
    }

    public void setLastAccessManager(LastAccessManager lastAccessManager)
    {
        this.lastAccessManager = lastAccessManager;
    }
}
