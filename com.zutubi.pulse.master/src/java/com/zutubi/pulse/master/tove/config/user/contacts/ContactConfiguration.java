package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Constraint;

/**
 * Base for all contact points, establishes the minimal contract they must
 * implement.
 */
@SymbolicName("zutubi.userContactConfig")
@Table(columns = {"name", "uid"})
@Classification(collection = "contacts")
public abstract class ContactConfiguration extends AbstractNamedConfiguration
{
    @Internal @Constraint("OnePrimaryContactValidator")
    private boolean primary;
    
    @Transient
    public abstract String getUid();

    public boolean isPrimary()
    {
        return primary;
    }

    public void setPrimary(boolean primary)
    {
        this.primary = primary;
        
        // Marking permanent when primary makes the contact undeletable and
        // uncloneable (both desirable, the latter as the clone would fail to
        // validate due to it being the second primary contact).
        setPermanent(primary);
    }

    public abstract void notify(BuildResult buildResult, String subject, String content, String mimeType) throws Exception;
}
