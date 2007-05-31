package com.zutubi.pulse.prototype.config.user.contacts;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.config.annotations.Table;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;

/**
 *
 *
 */
@SymbolicName("internal.userContactConfig")
@Table(columns = {"name", "uid"})
public abstract class ContactConfiguration extends AbstractNamedConfiguration
{
    @Transient
    public abstract String getUid();
}
