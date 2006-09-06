package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.util.logging.Logger;
import org.hibernate.*;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.sql.SQLException;
import java.util.List;

public class HibernateBuildResultDao extends HibernateEntityDao<BuildResult> implements BuildResultDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class);
    private static final String SPEC_QUERY = "select distinct model.buildSpecification from BuildResult model";

    @Override
    public Class persistentClass()
    {
        return BuildResult.class;
    }

    public List<BuildResult> findLatestByProject(Project project, int max)
    {
        return findLatestByProject(project, 0, max);
    }

    public List<BuildResult> findLatestByProject(final Project project, final int first, final int max)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.project = :project and model.user = null order by model.number desc");
                queryObject.setEntity("project", project);
                queryObject.setFirstResult(first);
                queryObject.setMaxResults(max);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }

    public BuildResult findPreviousBuildResult(final BuildResult result)
    {
        return (BuildResult)getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.user = null and model.project = :project and model.number < :number order by model.number desc");
                queryObject.setEntity("project", result.getProject());
                queryObject.setLong("number", result.getNumber());
                queryObject.setMaxResults(1);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.uniqueResult();
            }
        });
    }

    public List<BuildResult> findLatestCompleted(final Project project, final String spec, final int max)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.user = null and model.project = :project and model.buildSpecification = :spec and model.stateName != :initial and model.stateName != :inProgress order by model.number desc");
                queryObject.setEntity("project", project);
                queryObject.setParameter("spec", spec);
                queryObject.setParameter("initial", ResultState.INITIAL.toString(), Hibernate.STRING);
                queryObject.setParameter("inProgress", ResultState.IN_PROGRESS.toString(), Hibernate.STRING);
                queryObject.setMaxResults(max);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }

    public List<BuildResult> findLatestByProject(final Project project, final ResultState[] states, final String spec, final int first, final int max)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, spec);
                criteria.setFirstResult(first);
                criteria.setMaxResults(max);
                criteria.addOrder(Order.desc("id"));
                return criteria.list();
            }
        });
    }

    public List<BuildResult> findOldestByProject(final Project project, final int max)
    {
        return findOldestByProject(project, 0, max);
    }

    public List<BuildResult> findOldestByProject(final Project project, final int first, final int max)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.user = null and model.project = :project and model.stateName != :initial and model.stateName != :inProgress order by model.number asc");
                queryObject.setEntity("project", project);
                queryObject.setParameter("initial", ResultState.INITIAL.toString(), Hibernate.STRING);
                queryObject.setParameter("inProgress", ResultState.IN_PROGRESS.toString(), Hibernate.STRING);
                queryObject.setFirstResult(first);
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
                Query queryObject = session.createQuery("from BuildResult model where model.user = null and model.project = :project and model.number = :number");
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
        return (CommandResult) findAnyType(id, CommandResult.class);
    }

    public RecipeResultNode findRecipeResultNode(long id)
    {
        return (RecipeResultNode) findAnyType(id, RecipeResultNode.class);
    }

    public RecipeResult findRecipeResult(long id)
    {
        return (RecipeResult) findAnyType(id, RecipeResult.class);
    }

    public int getBuildCount(final Project project, final ResultState[] states, final String spec)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, spec);
                criteria.setProjection(Projections.rowCount());
                return criteria.uniqueResult();
            }
        });
    }

    public int getBuildCount(final Project project, final ResultState[] states, final Boolean hasWorkDir)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, null);
                if(hasWorkDir != null)
                {
                    criteria.add(Expression.eq("hasWorkDir", hasWorkDir.booleanValue()));
                }
                criteria.setProjection(Projections.rowCount());
                return criteria.uniqueResult();
            }
        });
    }

    public List<String> findAllSpecifications(final Project project)
    {
        return (List<String>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery(SPEC_QUERY + " where model.project = :project");
                queryObject.setEntity("project", project);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public List<String> findAllSpecificationsForProjects(final Project[] projects)
    {
        return (List<String>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject;

                if(projects == null)
                {
                    queryObject = session.createQuery(SPEC_QUERY);
                }
                else
                {
                    queryObject = session.createQuery(SPEC_QUERY + " where model.project in (:projects)");
                    queryObject.setParameterList("projects", projects);
                }

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public List<BuildResult> queryBuilds(final Project[] projects, final ResultState[] states, final String[] specs, final long earliestStartTime, final long latestStartTime, final Boolean hasWorkDir, final int first, final int max, final boolean mostRecentFirst)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Expression.isNull("user"));
                addProjectsToCriteria(projects, criteria);
                addStatesToCriteria(states, criteria);
                addSpecsToCriteria(specs, criteria);
                addDatesToCriteria(earliestStartTime, latestStartTime, criteria);

                if(hasWorkDir != null)
                {
                    criteria.add(Expression.eq("hasWorkDir", hasWorkDir.booleanValue()));
                }

                if(first >= 0)
                {
                    criteria.setFirstResult(first);
                }

                if(max >= 0)
                {
                    criteria.setMaxResults(max);
                }

                if(mostRecentFirst)
                {
                    criteria.addOrder(Order.desc("number"));
                }
                else
                {
                    criteria.addOrder(Order.asc("number"));
                }

                return criteria.list();
            }
        });
    }

    public List<BuildResult> querySpecificationBuilds(final Project project, final String spec, final ResultState[] states, final long lowestNumber, final long highestNumber, final int first, final int max, final boolean mostRecentFirst, boolean initialise)
    {
        List<BuildResult> results =  (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Expression.isNull("user"));
                criteria.add(Expression.eq("project", project));
                criteria.add(Expression.eq("buildSpecification", spec));
                addStatesToCriteria(states, criteria);
                addNumbersToCriteria(lowestNumber, highestNumber, criteria);

                if(first >= 0)
                {
                    criteria.setFirstResult(first);
                }

                if(max >= 0)
                {
                    criteria.setMaxResults(max);
                }

                if(mostRecentFirst)
                {
                    criteria.addOrder(Order.desc("number"));
                }
                else
                {
                    criteria.addOrder(Order.asc("number"));
                }

                return criteria.list();
            }
        });

        if(initialise)
        {
            for(BuildResult result: results)
            {
                intialise(result);
            }
        }

        return results;
    }

    public List<BuildResult> findByUser(final User user)
    {
        return getLatestBuildResultsForUser(user, -1);
    }

    public List<BuildResult> getLatestBuildResultsForUser(final User user, final int max)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.user = :user order by model.number desc");
                queryObject.setEntity("user", user);
                if(max > 0)
                {
                    queryObject.setMaxResults(max);
                }
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    private void intialise(BuildResult result)
    {
        Hibernate.initialize(result.getFeatures());
        for(RecipeResultNode node: result)
        {
            RecipeResult recipe = node.getResult();
            Hibernate.initialize(recipe.getFeatures());
            for(CommandResult command: recipe.getCommandResults())
            {
                Hibernate.initialize(command.getFeatures());
                for(StoredArtifact artifact: command.getArtifacts())
                {
                    for(StoredFileArtifact file: artifact.getChildren())
                    {
                        Hibernate.initialize(file.getFeatures());
                        Hibernate.initialize(file.getTests());
                    }
                }
            }
        }
    }

    private Criteria getBuildResultCriteria(Session session, Project project, ResultState[] states, String spec)
    {
        Criteria criteria = session.createCriteria(BuildResult.class);
        criteria.add(Expression.isNull("user"));
        criteria.add(Expression.eq("project", project));

        addStatesToCriteria(states, criteria);

        if (spec != null)
        {
            criteria.add(Expression.eq("buildSpecification", spec));
        }

        SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
        return criteria;
    }

    private void addProjectsToCriteria(Project[] projects, Criteria criteria)
    {
        if(projects != null)
        {
            criteria.add(Expression.in("project", projects));
        }
    }

    private void addStatesToCriteria(ResultState[] states, Criteria criteria)
    {
        if (states != null)
        {
            String[] stateNames = new String[states.length];
            for (int i = 0; i < states.length; i++)
            {
                stateNames[i] = states[i].toString();
            }

            criteria.add(Expression.in("stateName", stateNames));
        }
    }

    private void addSpecsToCriteria(String[] specs, Criteria criteria)
    {
        if(specs != null)
        {
            criteria.add(Expression.in("buildSpecification", specs));
        }
    }

    private void addDatesToCriteria(long earliestStartTime, long latestStartTime, Criteria criteria)
    {
        if(earliestStartTime > 0)
        {
            criteria.add(Expression.ge("stamps.startTime", earliestStartTime));
        }

        if(latestStartTime > 0)
        {
            // CIB-446: Don't accept timestamps that are uninitialised
            criteria.add(Expression.ge("stamps.startTime", 0L));
            criteria.add(Expression.le("stamps.startTime", latestStartTime));
        }
    }

    private void addNumbersToCriteria(long lowestNumber, long highestNumber, Criteria criteria)
    {
        if(lowestNumber > 0)
        {
            criteria.add(Expression.ge("number", lowestNumber));
        }

        if(highestNumber > 0)
        {
            criteria.add(Expression.le("number", highestNumber));
        }
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
