package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.config.annotations.Classification;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.master.model.BuildResult;

/**
 * Base for all contact points, establishes the minimal contract they must
 * implement.
 */
@SymbolicName("zutubi.userContactConfig")
@Table(columns = {"name", "uid"})
@Classification(collection = "contacts")
public abstract class ContactConfiguration extends AbstractNamedConfiguration
{
    @Transient
    public abstract String getUid();

    public abstract void notify(BuildResult buildResult, String subject, String content, String mimeType) throws Exception;
}
