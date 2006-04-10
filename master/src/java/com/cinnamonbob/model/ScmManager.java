package com.zutubi.pulse.model;

import java.util.List;

/**
 *
 * 
 */
public interface ScmManager extends EntityManager<Scm>
{
    Scm getScm(long id);

    List<Scm> getActiveScms();
}
