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

package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.scheduling.CallbackService;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Records last access times for users.  The purpose of this class is to
 * optimise the recording of access times - the record method is trivial, and
 * the persistence work is done out of line at regular intervals.
 */
public class LastAccessManager
{
    private static final Logger LOG = Logger.getLogger(LastAccessManager.class);

    public static final int ACCESS_NEVER = 0;
    public static final long ACTIVE_INTERVAL = 10 * Constants.MINUTE;

    private static final long FLUSH_INTERVAL = 5 * Constants.MINUTE;
    private static final String CALLBACK_NAME = "Last Access";

    private final Map<Long, Long> idToTime = new HashMap<Long, Long>();
    private CallbackService callbackService;
    private SessionFactory sessionFactory;
    private UserManager userManager;

    public void init()
    {
        try
        {
            callbackService.registerCallback(CALLBACK_NAME, new Runnable()
            {
                public void run()
                {
                    flush();
                }
            }, FLUSH_INTERVAL);
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    /**
     * Record an access right now by the user with the given id.
     *
     * @param id id of the user that accessed Pulse
     */
    public void recordAccess(long id)
    {
        synchronized (idToTime)
        {
            idToTime.put(id, System.currentTimeMillis());
        }
    }

    /**
     * Returns the last access time for the given user.
     *
     * @param id database id of the user
     * @return last access time for the user: zero for never
     */
    public long getLastAccessTime(long id)
    {
        Long cached;
        synchronized (idToTime)
        {
            cached = idToTime.get(id);
        }

        if (cached == null)
        {
            User user = userManager.getUser(id);
            if (user == null)
            {
                return ACCESS_NEVER;
            }
            else
            {
                long time = user.getLastAccessTime();
                synchronized (idToTime)
                {
                    idToTime.put(id, time);
                }

                return time;
            }
        }
        else
        {
            return cached;
        }
    }

    /**
     * Returns true if the given user has been active recently with respect to
     * the given current time.
     *
     * @param userId      id of the user to check
     * @param currentTime the current time in milliseconds since the Unix epoch
     *                    (passed in for efficiency reasons)
     * @return true if the user is active
     */
    public boolean isActive(long userId, long currentTime)
    {
        return currentTime - getLastAccessTime(userId) < ACTIVE_INTERVAL;
    }

    private void flush()
    {
        Map<Long, Long> copy;
        synchronized (idToTime)
        {
            copy = new HashMap<Long, Long>(idToTime);
        }

        Session session = sessionFactory.openSession();
        try
        {
            for (Map.Entry<Long, Long> entry : copy.entrySet())
            {
                User user = userManager.getUser(entry.getKey());
                if (user != null && user.getLastAccessTime() != entry.getValue())
                {
                    user.setLastAccessTime(entry.getValue());
                    userManager.save(user);
                }
            }
        }
        finally
        {
            session.close();
        }
    }

    public void setCallbackService(CallbackService callbackService)
    {
        this.callbackService = callbackService;
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
