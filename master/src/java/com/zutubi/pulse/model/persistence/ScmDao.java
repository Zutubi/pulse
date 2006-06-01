package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.Scm;

import java.util.List;

/**
 * 
 *
 */
public interface ScmDao extends EntityDao<Scm>
{
    List<Scm> findAllActive();
}
