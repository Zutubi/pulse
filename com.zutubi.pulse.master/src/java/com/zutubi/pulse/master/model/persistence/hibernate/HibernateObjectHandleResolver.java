package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.persistence.ObjectHandle;
import com.zutubi.pulse.master.model.persistence.ObjectHandleResolver;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.loader.Loader;
import org.hibernate.loader.entity.EntityLoader;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.OuterJoinLoadable;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Daniel Ostermeier
 */
public class HibernateObjectHandleResolver extends ObjectHandleResolver
{
    private SessionImplementor session;
    private SessionFactoryImplementor factory;

    public Object resolve(ObjectHandle handle)
    {
        return session.immediateLoad(handle.clazz.getName(), handle.id);
    }

    public Object[] resolve(ObjectHandle[] handles)
    {
        if (handles.length == 0)
            return new Object[0];

        Serializable[] ids = extractIds(handles);

        // initialise the entity loader
        Class persistentType = handles[0].clazz;
        ClassMetadata metaData = factory.getClassMetadata(persistentType);
        EntityPersister persister = factory.getEntityPersister(metaData.getEntityName());
        Loader loader = new EntityLoader((OuterJoinLoadable) persister, handles.length, LockMode.READ, factory, Collections.EMPTY_MAP);

        List objects = loader.loadEntityBatch(session, ids, persister.getIdentifierType(), null, null, null, persister);
        return objects.toArray(new Object[objects.size()]);
    }

    private Serializable[] extractIds(ObjectHandle[] handles)
    {
        List<Serializable> ids = new LinkedList<Serializable>();
        for (int i = 0; i < handles.length; i++)
        {
            ids.add(handles[i].id);
        }
        return ids.toArray(new Serializable[ids.size()]);
    }

    //---( required resources )---

    public void setSession(Session session)
    {
        this.session = (SessionImplementor) session;
    }

    public void setSessionFactory(SessionFactory factory)
    {
        this.factory = (SessionFactoryImplementor) factory;
    }
}