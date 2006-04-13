/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.persistence.AnyTypeDao;
import com.zutubi.pulse.model.persistence.ObjectHandle;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.EntityMode;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;
import java.util.Collection;
import java.util.LinkedList;
import java.io.Serializable;

/**
 * <class-comment/>
 */
public class HibernateAnyTypeDao extends HibernateDaoSupport implements AnyTypeDao
{
    public void save(Object entity)
    {
        getHibernateTemplate().saveOrUpdate(entity);
    }

    public List<ObjectHandle> findAll()
    {
        List<ObjectHandle> handles = new LinkedList<ObjectHandle>();
        SessionFactoryImplementor factory = getSessionFactoryImplementor();
        for (ClassMetadata metaData : (Collection<ClassMetadata>)factory.getAllClassMetadata().values())
        {
            handles.addAll(loadHandles(metaData));
        }
        return handles;
    }

    public List<ObjectHandle> findAll(Class persistentClass)
    {
        SessionFactoryImplementor factory = getSessionFactoryImplementor();
        ClassMetadata metaData = factory.getClassMetadata(persistentClass);
        return loadHandles(metaData);
    }

//TODO: What happens when the identifier covers multiple properties?
//      For now, we only have single property identifiers, so no need to worry.
//                Type identifierType = metaData.getIdentifierType();
//                if (identifierType instanceof ComponentType)
//                {
//                    ComponentType componentIdentifierType = (ComponentType) identifierType;
//                    componentIdentifierType.getPropertyNames();
//                }
//                else
//                {
//
//                }
    private List<ObjectHandle> loadHandles(ClassMetadata metaData)
    {
        List<ObjectHandle> handles = new LinkedList<ObjectHandle>();
        if (metaData.hasIdentifierProperty())
        {
            String propertyName = metaData.getIdentifierPropertyName();
            Class clazz = metaData.getMappedClass(EntityMode.POJO);

            List ids = getSession().createCriteria(clazz).setProjection(Projections.property(propertyName)).list();

            // convert ids into handles.
            for (Object id : ids)
            {
                handles.add(new ObjectHandle((Serializable)id, clazz));
            }
        }
        return handles;
    }

    private SessionFactoryImplementor getSessionFactoryImplementor()
    {
        return (SessionFactoryImplementor) getSessionFactory();
    }
}
