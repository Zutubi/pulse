package com.zutubi.pulse.prototype.config.user;

import com.zutubi.prototype.Formatter;

/**
 *
 *
 */
public class UserAliasConfigurationFormatter implements Formatter<UserAliasConfiguration>
{
    public String[] columns()
    {
        return new String[]{"alias"};
    }

    public String format(UserAliasConfiguration obj)
    {
        return obj.getAlias();
    }
}
