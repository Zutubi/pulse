package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.config.ScmConfiguration;

public interface ScmContextFactory
{
    ScmContext createContext(long projectId, ScmConfiguration scm) throws ScmException;
}
