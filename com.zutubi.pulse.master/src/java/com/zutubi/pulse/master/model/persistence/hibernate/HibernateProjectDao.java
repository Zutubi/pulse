package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateCallback;

import java.util.List;

/**
 * Hibernate implementation of {@link ProjectDao}.
 */
public class HibernateProjectDao extends HibernateEntityDao<Project> implements ProjectDao
{
    public Class<Project> persistentClass()
    {
        return Project.class;
    }

    @SuppressWarnings({"unchecked"})
    public List<Project> findByResponsible(final User user)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<Project>>()
        {
            public List<Project> doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from Project project where responsibility.user = :user");
                queryObject.setEntity("user", user);
                return queryObject.list();
            }
        });
    }
}
