package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

public class UserConfigurations
{
    public UserConfiguration createSimpleUser(String userName)
    {
        UserConfiguration user = new UserConfiguration(userName, userName);
        user.setPassword("");
        return user;
    }
}
