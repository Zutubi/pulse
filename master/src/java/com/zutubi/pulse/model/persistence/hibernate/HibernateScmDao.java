/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.model.persistence.ScmDao;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * 
 *
 */
public class HibernateScmDao extends HibernateEntityDao<Scm> implements ScmDao
{
    @Override
    public Class persistentClass()
    {
        return Scm.class;
    }

    public List<Scm> findAllActive()
    {
        return (List<Scm>)getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Criteria criteria = session.createCriteria(Scm.class);
                criteria.add(Expression.eq("monitor", true));
                SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
                return criteria.list();
            }
        });
    }
}
