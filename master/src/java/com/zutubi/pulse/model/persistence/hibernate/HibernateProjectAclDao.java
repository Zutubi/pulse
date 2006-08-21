package com.zutubi.pulse.model.persistence.hibernate;

import org.acegisecurity.acl.basic.BasicAclEntry;
import org.acegisecurity.acl.basic.AclObjectIdentity;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Query;

import java.util.List;

import com.zutubi.pulse.model.ProjectAclEntry;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.ProjectAclDao;

/**
 */
public class HibernateProjectAclDao extends HibernateDaoSupport implements ProjectAclDao
{
    public Class persistentClass()
    {
        return ProjectAclEntry.class;
    }

    public BasicAclEntry[] getAcls(final AclObjectIdentity aclObjectIdentity)
    {
        List<ProjectAclEntry> entries = (List<ProjectAclEntry>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from ProjectAclEntry model where model.aclObjectIdentity = :project");
                queryObject.setEntity("project", aclObjectIdentity);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });

        entries.add(new ProjectAclEntry(GrantedAuthority.ADMINISTRATOR, (Project) aclObjectIdentity, ProjectAclEntry.ADMINISTRATION));
        entries.add(new ProjectAclEntry(GrantedAuthority.USER, (Project) aclObjectIdentity, ProjectAclEntry.READ));

        return entries.toArray(new BasicAclEntry[entries.size()]);
    }

    public List<ProjectAclEntry> findByRecipient(final String recipient)
    {
        return (List<ProjectAclEntry>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from ProjectAclEntry model where model.recipient = :recipient");
                queryObject.setString("recipient", recipient);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }
}
