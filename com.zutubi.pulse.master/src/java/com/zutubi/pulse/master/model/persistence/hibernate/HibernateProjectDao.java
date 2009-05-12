package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;

/**
 */
public class HibernateProjectDao extends HibernateEntityDao<Project> implements ProjectDao
{
    public Class persistentClass()
    {
        return Project.class;
    }

    @SuppressWarnings({"unchecked"})
    public List<Project> findByResponsible(final User user)
    {
        return (List<Project>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from Project project where responsibility.user = :user");
                queryObject.setEntity("user", user);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }
}
