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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.UserManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Actions available for users.
 */
public class UserConfigurationActions
{
    public static final String ACTION_SET_PASSWORD = "setPassword";
    
    private UserManager userManager;

    public List<String> getActions(UserConfiguration user)
    {
        if(user.isAuthenticatedViaLdap())
        {
            return Collections.emptyList();
        }
        else
        {
            return Arrays.asList(ACTION_SET_PASSWORD);
        }
    }

    public void doSetPassword(UserConfiguration user, SetPasswordConfiguration password)
    {
        userManager.setPassword(user, password.getPassword());
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
