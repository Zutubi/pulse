package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.logging.Logger;
import org.hibernate.*;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"unchecked"})
public class HibernateBuildResultDao extends HibernateEntityDao<BuildResult> implements BuildResultDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class);

    @Override
    public Class persistentClass()
    {
        return BuildResult.class;
    }

    public List<BuildResult> findLatestByProject(Project project, int max)
    {
        return findLatestByProject(project, 0, max);
    }

    public List<BuildResult> findSinceByProject(final Project project, final Date since)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.project = :project and model.user = null and model.stamps.endTime > :since order by model.number desc");
                queryObject.setEntity("project", project);
                queryObject.setLong("since", since.getTime());

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
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
        return (BuildResult) getHibernateTemplate().execute(new HibernateCallback()
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

    public BuildResult findPreviousBuildResultWithRevision(final BuildResult result, final ResultState[] states)
    {
        return (BuildResult)getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, result.getProject(), states, false);
                criteria.add(Expression.lt("number", result.getNumber()));
                criteria.add(Expression.eq("userRevision", false));
                criteria.add(Expression.isNotNull("revisionString"));
                criteria.setMaxResults(1);
                criteria.addOrder(Order.desc("number"));
                return criteria.uniqueResult();
            }
        });
    }

    public List<BuildResult> findLatestCompleted(final Project project, final int first, final int max)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, ResultState.getCompletedStates(), false);
                criteria.setFirstResult(first);
                criteria.setMaxResults(max);
                criteria.addOrder(Order.desc("number"));
                SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());

                return criteria.list();
            }
        });
    }

    public List<BuildResult> findLatestByProject(final Project project, final ResultState[] states, final int first, final int max)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, false);
                criteria.setFirstResult(first);
                criteria.setMaxResults(max);
                criteria.addOrder(Order.desc("id"));
                return criteria.list();
            }
        });
    }

    public List<BuildResult> findOldestByProject(final Project project, final ResultState[] states, final int max, final boolean includePersonal)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, includePersonal);
                criteria.setMaxResults(max);
                criteria.addOrder(Order.asc("id"));
                return criteria.list();
            }
        });
    }

    public BuildResult findByProjectAndMetabuildId(final Project project, final long metaBuildId, final ResultState... states)
    {
        return (BuildResult)getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, false);
                criteria.add(Expression.eq("metaBuildId", metaBuildId));
                criteria.setMaxResults(1);
                criteria.addOrder(Order.desc("number"));
                return criteria.uniqueResult();
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

    public BuildResult findByUserAndNumber(final User user, final long number)
    {
        List results = (List) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.user = :user and model.number = :number");
                queryObject.setEntity("user", user);
                queryObject.setParameter("number", number, Hibernate.LONG);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });

        if (results.size() > 1)
        {
            LOG.warning("findByUserAndNumber has returned " + results.size() +
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

    public int getBuildCount(final Project project, final ResultState[] states)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, false);
                criteria.setProjection(Projections.rowCount());
                return criteria.uniqueResult();
            }
        });
    }

    public int getBuildCount(final Project project, final ResultState[] states, final String[] statuses)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, false);
                addStatusesToCriteria(statuses, criteria);
                criteria.setProjection(Projections.rowCount());
                return criteria.uniqueResult();
            }
        });
    }

    public int getBuildCount(final Project project, final long after, final long upTo)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, null, null, false);
                criteria.add(Expression.gt("number", after));
                criteria.add(Expression.le("number", upTo));
                criteria.setProjection(Projections.rowCount());
                return criteria.uniqueResult();
            }
        });
    }

    public List<BuildResult> queryBuilds(final Project[] projects, final ResultState[] states, final long earliestStartTime, final long latestStartTime, final int first, final int max, final boolean mostRecentFirst)
    {
        return queryBuilds(projects, states, null, earliestStartTime, latestStartTime, first, max, mostRecentFirst);
    }

    public List<BuildResult> queryBuilds(final Project[] projects, final ResultState[] states, final String[] statuses, final long earliestStartTime, final long latestStartTime, final int first, final int max, final boolean mostRecentFirst)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Expression.isNull("user"));
                addProjectsToCriteria(projects, criteria);
                addStatesToCriteria(states, criteria);
                addStatusesToCriteria(statuses, criteria);
                addDatesToCriteria(earliestStartTime, latestStartTime, criteria);

                if (first >= 0)
                {
                    criteria.setFirstResult(first);
                }

                if (max >= 0)
                {
                    criteria.setMaxResults(max);
                }

                if (mostRecentFirst)
                {
                    criteria.addOrder(Order.desc("id"));
                }
                else
                {
                    criteria.addOrder(Order.asc("id"));
                }

                return criteria.list();
            }
        });
    }

    public List<BuildResult> queryBuilds(final Project project, final ResultState[] states, final long lowestNumber, final long highestNumber, final int first, final int max, final boolean mostRecentFirst, boolean initialise)
    {
        List<BuildResult> results = (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Expression.isNull("user"));
                criteria.add(Expression.eq("project", project));
                addStatesToCriteria(states, criteria);
                addNumbersToCriteria(lowestNumber, highestNumber, criteria);

                if (first >= 0)
                {
                    criteria.setFirstResult(first);
                }

                if (max >= 0)
                {
                    criteria.setMaxResults(max);
                }

                if (mostRecentFirst)
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

        if (initialise)
        {
            for (BuildResult result : results)
            {
                initialise(result);
            }
        }

        return results;
    }

    public List<BuildResult> queryBuildsWithMessages(final Project[] projects, final Feature.Level level, final int max)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Expression.isNull("user"));
                addProjectsToCriteria(projects, criteria);
                criteria.add(Expression.gt(level.toString().toLowerCase() + "FeatureCount", 0));

                if (max >= 0)
                {
                    criteria.setMaxResults(max);
                }

                criteria.addOrder(Order.desc("id"));
                return criteria.list();
            }
        });
    }

    public List<BuildResult> findByUser(final User user)
    {
        return getLatestByUser(user, null, -1);
    }

    public List<BuildResult> getLatestByUser(final User user, final ResultState[] states, final int max)
    {
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Expression.eq("user", user));

                if (states != null)
                {
                    criteria.add(Expression.in("stateName", getStateNames(states)));
                }

                if (max > 0)
                {
                    criteria.setMaxResults(max);
                }

                criteria.addOrder(Order.desc("number"));

                return criteria.list();
            }
        });
    }

    public int getCompletedResultCount(final User user)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("select count (*) from BuildResult model where model.user = :user and model.stateName in (:stateNames)");
                queryObject.setEntity("user", user);
                queryObject.setParameterList("stateNames", getStateNames(ResultState.getCompletedStates()));
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.uniqueResult();
            }
        });
    }

    public List<BuildResult> getOldestCompletedBuilds(final User user, final int offset)
    {
        int count = getCompletedResultCount(user);
        if (count > offset)
        {
            final int max = count - offset;

            return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
            {
                public Object doInHibernate(Session session) throws HibernateException
                {
                    Query queryObject = session.createQuery("from BuildResult model where model.user = :user and model.stateName in (:stateNames) order by model.number asc");
                    queryObject.setEntity("user", user);
                    queryObject.setParameterList("stateNames", getStateNames(ResultState.getCompletedStates()));
                    if (max > 0)
                    {
                        queryObject.setMaxResults(max);
                    }
                    SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                    return queryObject.list();
                }
            });
        }

        return Collections.emptyList();
    }

    public List<BuildResult> getOldestBuilds(Project project, ResultState[] states, Boolean hasWorkDir, int limit)
    {
        int total = getBuildCount(project, states, null);
        if (total > limit)
        {
            // Clean out the difference
            return queryBuilds(new Project[]{project}, states, 0, 0, 0, total - limit, false);
        }

        return Collections.emptyList();
    }

    public RecipeResultNode findResultNodeByResultId(long id)
    {
        return (RecipeResultNode) findUniqueByNamedQuery("findResultNodeByResultId", "id", id, true);
    }

    public BuildResult findLatest()
    {
        return (BuildResult) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from BuildResult result order by result.stamps.endTime desc");
                queryObject.setMaxResults(1);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.uniqueResult();
            }
        });
    }

    public CommandResult findCommandResultByArtifact(final long artifactId)
    {
        return (CommandResult) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("SELECT result " +
                        "FROM CommandResult result " +
                        "JOIN result.artifacts artifact " +
                        "WHERE artifact.id = :id");
                queryObject.setLong("id", artifactId);
                queryObject.setMaxResults(1);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.uniqueResult();
            }
        });
    }

    public BuildResult findLatestSuccessfulByProject(Project project)
    {
        BuildResult result = (BuildResult) findFirstByNamedQuery("findLatestSuccessfulByProject", "project", project, false);
        if (result != null)
        {
            initialise(result);
        }
        return result;
    }

    public BuildResult findLatestSuccessful()
    {
        return (BuildResult) findFirstByNamedQuery("findLatestSuccessful", false);
    }

    public BuildResult findByRecipeId(long id)
    {
        final RecipeResultNode node = findResultNodeByResultId(id);
        if (node == null)
        {
            return null;
        }

        return (BuildResult) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("select result from BuildResult result join result.root.children child where child = :node");
                queryObject.setEntity("node", node);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.uniqueResult();
            }
        });
    }

    public List<BuildResult> findByBeforeBuild(final long buildId, final int maxResults, final ResultState... states)
    {
        final BuildResult result = findById(buildId);
        List<BuildResult> results = (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("SELECT result " +
                        "FROM BuildResult result " +
                        "WHERE result.id < :buildId " +
                        "AND result.project.id = :projectId " +
                        "AND result.stateName IN (:stateNames) " +
                        "ORDER BY result DESC"
                );
                queryObject.setLong("buildId", buildId);
                queryObject.setLong("projectId", result.getProject().getId());
                queryObject.setParameterList("stateNames", getStateNames(states.length != 0 ? states : ResultState.values()));
                queryObject.setMaxResults(maxResults);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
        return CollectionUtils.reverse(results);
    }

    public List<BuildResult> findByAfterBuild(final long buildId, final int maxResults, final ResultState... states)
    {
        final BuildResult result = findById(buildId);
        return (List<BuildResult>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("SELECT result " +
                        "FROM BuildResult result " +
                        "WHERE result.id > :buildId " +
                        "AND result.project.id = :projectId " +
                        "AND result.stateName IN (:stateNames) " +
                        "ORDER BY result ASC"
                );
                queryObject.setLong("buildId", buildId);
                queryObject.setLong("projectId", result.getProject().getId());
                queryObject.setParameterList("stateNames", getStateNames(states.length != 0 ? states : ResultState.values()));
                queryObject.setMaxResults(maxResults);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public static void initialise(final BuildResult result)
    {
        Hibernate.initialize(result.getFeatures());
        for (RecipeResultNode node : result)
        {
            RecipeResult recipe = node.getResult();
            Hibernate.initialize(recipe.getFeatures());
            for (CommandResult command : recipe.getCommandResults())
            {
                Hibernate.initialize(command.getFeatures());
                for (StoredArtifact artifact : command.getArtifacts())
                {
                    Hibernate.initialize(artifact.getChildren());
                }
            }
        }
    }

    private Criteria getBuildResultCriteria(Session session, Project project, ResultState[] states, boolean includePersonal)
    {
        Criteria criteria = session.createCriteria(BuildResult.class);
        if (!includePersonal)
        {
            criteria.add(Expression.isNull("user"));
        }

        if (project != null)
        {
            criteria.add(Expression.eq("project", project));
        }

        addStatesToCriteria(states, criteria);

        SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
        return criteria;
    }

    private void addProjectsToCriteria(Project[] projects, Criteria criteria)
    {
        if (projects != null && projects.length > 0)
        {
            criteria.add(Expression.in("project", projects));
        }
    }

    private void addStatesToCriteria(ResultState[] states, Criteria criteria)
    {
        if (states != null && states.length > 0)
        {
            criteria.add(Expression.in("stateName", getStateNames(states)));
        }
    }

    private void addStatusesToCriteria(String[] statuses, Criteria criteria)
    {
        if (statuses != null && statuses.length > 0)
        {
            criteria.add(Expression.in("status", statuses));
        }
    }

    private String[] getStateNames(ResultState[] states)
    {
        String[] stateNames = new String[states.length];
        for (int i = 0; i < states.length; i++)
        {
            stateNames[i] = states[i].toString();
        }
        return stateNames;
    }

    private void addDatesToCriteria(long earliestStartTime, long latestStartTime, Criteria criteria)
    {
        if (earliestStartTime > 0)
        {
            criteria.add(Expression.ge("stamps.startTime", earliestStartTime));
        }

        if (latestStartTime > 0)
        {
            // CIB-446: Don't accept timestamps that are uninitialised
            criteria.add(Expression.ge("stamps.startTime", 0L));
            criteria.add(Expression.le("stamps.startTime", latestStartTime));
        }
    }

    private void addNumbersToCriteria(long lowestNumber, long highestNumber, Criteria criteria)
    {
        if (lowestNumber > 0)
        {
            criteria.add(Expression.ge("number", lowestNumber));
        }

        if (highestNumber > 0)
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
