package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

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
