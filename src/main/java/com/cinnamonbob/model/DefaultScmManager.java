package com.cinnamonbob.model;

import com.cinnamonbob.model.persistence.ScmDao;

/**
 *
 *
 */
public class DefaultScmManager implements ScmManager
{
    private ScmDao scmDao = null;

    public void setScmDao(ScmDao scmDao)
    {
        this.scmDao = scmDao;
    }

    public void save(Scm entity)
    {
        scmDao.save(entity);
    }

    public void delete(Scm entity)
    {
        scmDao.delete(entity);
    }
}
