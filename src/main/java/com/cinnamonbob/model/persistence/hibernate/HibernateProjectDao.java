package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.persistence.ProjectDao;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;
import java.util.logging.Logger;

/**
 * 
 *
 */
public class HibernateProjectDao extends HibernateEntityDao<Project> implements ProjectDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class.getName());

    @Override
    public Class persistentClass()
    {
        return Project.class;
    }

    public Project findByName(final String name)
    {
        return findUniqueByNamedQuery("project.findByName", "name", name, false);
    }

    public List<Project> findByLikeName(final String name)
    {
        return (List<Project>)findByNamedQuery("project.findByLikeName", "name", name);
    }
}