package com.zutubi.pulse.prototype.config.project.commit;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.pulse.committransformers.Substitution;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;

import java.util.List;

/**
 */
@SymbolicName("zutubi.commitMessageTransformerConfig")
@Table(columns = "name")
public abstract class CommitMessageTransformerConfiguration extends AbstractNamedConfiguration
{
    private boolean exclusive = false;

    public abstract List<Substitution> substitutions();

    public boolean isExclusive()
    {
        return exclusive;
    }

    public void setExclusive(boolean exclusive)
    {
        this.exclusive = exclusive;
    }
}
