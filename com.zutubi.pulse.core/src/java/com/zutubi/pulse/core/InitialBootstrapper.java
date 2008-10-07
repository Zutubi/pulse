package com.zutubi.pulse.core;

import com.zutubi.pulse.core.scm.api.Revision;

/**
 * <class-comment/>
 */
public interface InitialBootstrapper extends Bootstrapper
{
    Revision getRevision();
}
