package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.RecipeResultNode;
import com.cinnamonbob.model.persistence.BuildResultDao;
import com.cinnamonbob.util.logging.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.sql.SQLException;
import java.util.List;

public class HibernateBuildResultDao extends HibernateEntityDao<BuildResult> implements BuildResultDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class);

    @Override
    public Class persistentClass()
    {
        return BuildResult.class;
    }

    public List findLatestByProject(final Project project, final int max)
    {
        return (List) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.project = :project and model.stateName != :initial order by id desc");
                queryObject.setEntity("project", project);
                queryObject.setParameter("initial", ResultState.INITIAL.toString(), Hibernate.STRING);
                queryObject.setMaxResults(max);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }

    public BuildResult findByProjectAndNumber(final Project project, final long number)
    {
        List results = (List) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.project = :project and model.number = :number");
                queryObject.setEntity("project", project);
                queryObject.setParameter("number", number, Hibernate.LONG);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });

        if (results.size() > 1)
        {
            LOG.warning("findByProjectNameAndNumber has returned " + results.size() +
                    " results when expecting at most one.");
        }
        if (results.size() > 0)
        {
            return (BuildResult) results.get(0);
        }
        return null;
    }

    public CommandResult findCommandResult(long id)
    {
        return (CommandResult) getHibernateTemplate().load(CommandResult.class, Long.valueOf(id));
    }

    public RecipeResultNode findRecipeResultNode(long id)
    {
        return (RecipeResultNode) getHibernateTemplate().load(RecipeResultNode.class, Long.valueOf(id));
    }

    public RecipeResult findRecipeResult(long id)
    {
        return (RecipeResult) getHibernateTemplate().load(RecipeResult.class, Long.valueOf(id));
    }

    public void save(RecipeResultNode node)
    {
        getHibernateTemplate().saveOrUpdate(node);
    }

    public void save(RecipeResult result)
    {
        getHibernateTemplate().saveOrUpdate(result);
    }

    public void save(CommandResult result)
    {
        getHibernateTemplate().saveOrUpdate(result);
    }

}
