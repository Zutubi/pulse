package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.security.LastAccessManager;
import com.zutubi.pulse.master.tove.format.MessagesAware;
import com.zutubi.util.TimeStamps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * State fields for {@link com.zutubi.pulse.master.tove.config.user.UserConfiguration}.
 */
public class UserConfigurationStateDisplay implements MessagesAware
{
    private static final DateFormat LAST_ACCESS_FORMAT = SimpleDateFormat.getDateTimeInstance();

    private LastAccessManager lastAccessManager;
    private Messages messages;

    /**
     * Shows the last access time for this user: i.e. the last time they
     * accessed the Pulse interface.
     *
     * @param userConfig the user in question
     * @return a human-friendly description of when the user last accessed
     *         Pulse
     */
    public String formatLastAccess(UserConfiguration userConfig)
    {
        long time = lastAccessManager.getLastAccessTime(userConfig.getUserId());
        if (time != LastAccessManager.ACCESS_NEVER)
        {
            String dayAndTime;
            synchronized (LAST_ACCESS_FORMAT)
            {
                dayAndTime = LAST_ACCESS_FORMAT.format(new Date(time));
            }

            return messages.format("lastAccess.format", new Object[]{ dayAndTime, TimeStamps.getPrettyElapsed(System.currentTimeMillis() - time)});
        }

        return messages.format("lastAccess.never");
    }

    /**
     * Shows the number of users in the given collection that have accessed
     * Pulse recently.
     *
     * @param users the users in question
     * @return a human-friendly description of which of the users are active
     */
    public String formatCollectionActiveCount(Collection<UserConfiguration> users)
    {
        int count = 0;
        long currentTime = System.currentTimeMillis();
        for (UserConfiguration user: users)
        {
            if (lastAccessManager.isActive(user.getUserId(), currentTime))
            {
                count++;
            }
        }

        return messages.format("activeCount.format", count, users.size());
    }

    public void setMessages(Messages messages)
    {
        this.messages = messages;
    }

    public void setLastAccessManager(LastAccessManager lastAccessManager)
    {
        this.lastAccessManager = lastAccessManager;
    }
}
