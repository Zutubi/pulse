package com.zutubi.pulse.core.scm.config.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Base for SCM configuration types.  All SCM plugins must support at least
 * this configuration.
 */
@SymbolicName("zutubi.scmConfig")
public abstract class ScmConfiguration extends AbstractConfiguration
{
    private List<CommitterMappingConfiguration> committerMappings = new LinkedList<CommitterMappingConfiguration>();

    public List<CommitterMappingConfiguration> getCommitterMappings()
    {
        return committerMappings;
    }

    public void setCommitterMappings(List<CommitterMappingConfiguration> committerMappings)
    {
        this.committerMappings = committerMappings;
    }

    /**
     * Returns a short type string used to identify the SCM type (e.g.
     * "svn"). This type may be used by other parts of the system to
     * determine which SCM they are dealing with.  For example change viewers
     * may use different strategies to deal with different SCMs.
     *
     * @return the SCM type
     */
    @Transient
    public abstract String getType();
}
