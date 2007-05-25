package com.zutubi.pulse.prototype.config.user;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Format;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
@SymbolicName("internal.userAliasConfig")
@Format("UserAliasConfigurationFormatter")
public class UserAliasConfiguration extends AbstractConfiguration
{
    private String alias;

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }
}
