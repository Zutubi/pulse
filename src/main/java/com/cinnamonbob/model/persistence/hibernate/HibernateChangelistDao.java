package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.persistence.ChangelistDao;
import com.cinnamonbob.model.SimpleChangelist;

/**
 * Created by IntelliJ IDEA.
 * User: daniel
 * Date: 14/08/2005
 * Time: 15:27:02
 * To change this template use File | Settings | File Templates.
 */
public class HibernateChangelistDao extends HibernateEntityDao<SimpleChangelist> implements ChangelistDao
{
    public Class persistentClass()
    {
        return SimpleChangelist.class;
    }
}
