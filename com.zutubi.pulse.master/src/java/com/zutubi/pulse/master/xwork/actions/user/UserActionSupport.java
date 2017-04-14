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

package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.util.StringUtils;

import java.util.Arrays;

/**
 *
 * 
 */
public class UserActionSupport extends ActionSupport
{
    private MasterConfigurationManager configurationManager;

    /**
     * static long indicating that a value has not been specified.
     */
    public static final long NONE_SPECIFIED = -1;

    /**
     * User id.
     */
    private long userId = NONE_SPECIFIED;

    /**
     * User login.
     */
    private String userLogin = null;

    private User user = null;

    /**
     *
     * @return user id, or -1 if none specified.
     */
    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long user)
    {
        this.userId = user;
    }

    public String getUserLogin()
    {
        return userLogin;
    }

    public void setUserLogin(String userLogin)
    {
        this.userLogin = userLogin;
    }

    public User getUser()
    {
        if(user != null)
        {
            return user;
        }
        
        if (userId != NONE_SPECIFIED)
        {
            return getUser(userId);
        }
        else if (StringUtils.stringSet(userLogin))
        {
            return getUser(userLogin);
        }

        Object principle = getPrinciple();
        if(principle != null)
        {
            return getUser((String) principle);
        }
        return null;
    }

    protected User getUser(String userLogin)
    {
        return userManager.getUser(userLogin);
    }

    protected User getUser(long userId)
    {
        return userManager.getUser(userId);
    }

    protected void addUnknownUserActionError()
    {
        if (userId != NONE_SPECIFIED)
        {
            addActionError(getText("user.unknown", Arrays.asList(userId)));
        }
        else if (StringUtils.stringSet(userLogin))
        {
            addActionError(getText("user.unknown", Arrays.asList(userLogin)));
        }
    }

    protected void addUnknownUserFieldError()
    {
        if (userId != NONE_SPECIFIED)
        {
            addFieldError("userId", "Unknown user '" + userId + "'");
        }
        else if (StringUtils.stringSet(userLogin))
        {
            addFieldError("userLogin", "Unknown user '" + userLogin + "'");
        }
    }

    public MasterConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
