/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.persistence.PulseFileDetailsDao;

/**
 */
public class HibernatePulseFileDetailsDao extends HibernateEntityDao<PulseFileDetails> implements PulseFileDetailsDao
{
    public Class persistentClass()
    {
        return PulseFileDetails.class;
    }
}
