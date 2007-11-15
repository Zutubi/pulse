package com.zutubi.pulse.prototype.config.project.commit;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.core.config.NamedConfiguration;
import com.zutubi.pulse.committransformers.Substitution;

import java.util.List;

/**
 */
@SymbolicName("zutubi.commitMessageTransformerConfig")
public abstract class CommitMessageTransformerConfiguration extends AbstractNamedConfiguration
{
    public abstract List<Substitution> substitutions();
}
