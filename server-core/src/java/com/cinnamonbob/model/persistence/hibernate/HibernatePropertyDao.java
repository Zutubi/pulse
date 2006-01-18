package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.Property;
import com.cinnamonbob.model.persistence.PropertyDao;

/**
 */
public class HibernatePropertyDao extends HibernateEntityDao<Property> implements PropertyDao
{
    public Class persistentClass()
    {
        return Property.class;
    }
}
