/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.ContactPoint;
import com.zutubi.pulse.model.persistence.ContactPointDao;

/**
 *
 * 
 */
public class HibernateContactPointDao extends HibernateEntityDao<ContactPoint> implements ContactPointDao
{

    public Class persistentClass()
    {
        return ContactPoint.class;
    }
}
