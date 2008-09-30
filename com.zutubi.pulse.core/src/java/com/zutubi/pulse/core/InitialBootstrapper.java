package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Revision;

/**
 * <class-comment/>
 */
public interface InitialBootstrapper extends Bootstrapper
{
    Revision getRevision();
}
