package com.zutubi.pulse.master.tove.config.project.commit;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.AbstractNamedConfiguration;
import com.zutubi.pulse.master.committransformers.Substitution;

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
