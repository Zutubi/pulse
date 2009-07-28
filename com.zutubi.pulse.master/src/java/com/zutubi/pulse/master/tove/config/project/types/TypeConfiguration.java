package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.PulseFileSource;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * Defines what the pulse file looks like for a project.  It could be built
 * from a restricted template (e.g. an ant project) or could be had-written
 * by the user (e.g. a custom project) or anything else that can produce a
 * valid pulse file.
 */
@SymbolicName("zutubi.typeConfig")
public abstract class TypeConfiguration extends AbstractConfiguration
{
    @Transient
    public abstract PulseFileSource getPulseFile() throws Exception;
}
