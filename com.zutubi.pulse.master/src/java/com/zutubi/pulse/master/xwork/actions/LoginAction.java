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

package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * This login action is to provide mapping support between how Acegi expects the
 * data and how webwork likes to present it.
 */
public class LoginAction extends ActionSupport
{
    private boolean authenticationError = false;
    private String username;
    private boolean rememberMe;
    private boolean anonymousSignupEnabled;
    private ConfigurationProvider configurationProvider;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public boolean isRememberMe()
    {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe)
    {
        this.rememberMe = rememberMe;
    }

    public void setError(boolean error)
    {
        this.authenticationError = error;
    }

    @Override
    public String doInput() throws Exception
    {
        if (authenticationError)
        {
            addActionError(getText("login.badcredentials"));
        }

        GlobalConfiguration global = configurationProvider.get(GlobalConfiguration.class);
        anonymousSignupEnabled = global.isAnonymousSignupEnabled();

        return INPUT;
    }

    @Override
    public String execute() throws Exception
    {
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
