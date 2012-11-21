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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Hibernate-based implementation of {@link BuildResultDao}.
 */
@SuppressWarnings({"unchecked"})
public class HibernateBuildResultDao extends HibernateEntityDao<BuildResult> implements BuildResultDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class);

    @Override
    public Class<BuildResult> persistentClass()
    {
        return BuildResult.class;
    }

    public List<BuildResult> findLatestByProject(Project project, int max)
    {
        return findLatestByProject(project, 0, max);
    }

    public List<BuildResult> findSinceByProject(final Project project, final Date since)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
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
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
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
        return getHibernateTemplate().execute(new HibernateCallback<BuildResult>()
        {
            public BuildResult doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.user = null and model.project = :project and model.number < :number order by model.number desc");
                queryObject.setEntity("project", result.getProject());
                queryObject.setLong("number", result.getNumber());
                queryObject.setMaxResults(1);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return (BuildResult) queryObject.uniqueResult();
            }
        });
    }

    public BuildResult findPreviousBuildResultWithRevision(final BuildResult result, final ResultState[] states)
    {
        return getHibernateTemplate().execute(new HibernateCallback<BuildResult>()
        {
            public BuildResult doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, result.getProject(), states, false);
                criteria.add(Restrictions.lt("number", result.getNumber()));
                criteria.add(Restrictions.eq("userRevision", false));
                criteria.add(Restrictions.isNotNull("revisionString"));
                criteria.setMaxResults(1);
                criteria.addOrder(Order.desc("number"));
                return (BuildResult) criteria.uniqueResult();
            }
        });
    }

    public List<BuildResult> findLatestCompleted(final Project project, final int first, final int max)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
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

    public List<BuildResult> findCompletedSince(final Project[] projects, final long sinceTime)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Restrictions.isNull("user"));
                addProjectsToCriteria(projects, criteria);
                criteria.add(Restrictions.gt("stamps.endTime", sinceTime));
                criteria.addOrder(Order.desc("id"));
                return criteria.list();
            }
        });
    }

    public List<BuildResult> findLatestByProject(final Project project, final ResultState[] states, final int first, final int max)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
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
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
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
        return getHibernateTemplate().execute(new HibernateCallback<BuildResult>()
        {
            public BuildResult doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, false);
                criteria.add(Restrictions.eq("metaBuildId", metaBuildId));
                criteria.setMaxResults(1);
                criteria.addOrder(Order.desc("number"));
                return (BuildResult) criteria.uniqueResult();
            }
        });
    }

    public BuildResult findByProjectAndNumber(final long projectId, final long number)
    {
        List<BuildResult> results = getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.user = null and model.project.id = :project and model.number = :number");
                queryObject.setLong("project", projectId);
                queryObject.setParameter("number", number);

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
            return results.get(0);
        }
        return null;
    }

    public BuildResult findByUserAndNumber(final User user, final long number)
    {
        List<BuildResult> results = (List) getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("from BuildResult model where model.user = :user and model.number = :number");
                queryObject.setEntity("user", user);
                queryObject.setParameter("number", number);

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
            return results.get(0);
        }
        return null;
    }

    public CommandResult findCommandResult(long id)
    {
        return findAnyType(id, CommandResult.class);
    }

    public RecipeResult findRecipeResult(long id)
    {
        return findAnyType(id, RecipeResult.class);
    }

    public int getBuildCount(final Project project, final ResultState[] states)
    {
        return getHibernateTemplate().execute(new HibernateCallback<Integer>()
        {
            public Integer doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, false);
                criteria.setProjection(Projections.rowCount());
                return (Integer) criteria.uniqueResult();
            }
        });
    }

    public int getBuildCount(final Project project, final ResultState[] states, final String[] statuses, final boolean includePinned)
    {
        return getHibernateTemplate().execute(new HibernateCallback<Integer>()
        {
            public Integer doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, states, false);
                if (!includePinned)
                {
                    criteria.add(Restrictions.eq("pinned", false));
                }
                addStatusesToCriteria(statuses, criteria);
                criteria.setProjection(Projections.rowCount());
                return (Integer) criteria.uniqueResult();
            }
        });
    }

    public int getBuildCount(final Project project, final long after, final long upTo)
    {
        return getHibernateTemplate().execute(new HibernateCallback<Integer>()
        {
            public Integer doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, project, null, false);
                criteria.add(Restrictions.gt("number", after));
                criteria.add(Restrictions.le("number", upTo));
                criteria.setProjection(Projections.rowCount());
                return (Integer) criteria.uniqueResult();
            }
        });
    }

    public List<BuildResult> queryBuilds(final Project[] projects, final ResultState[] states, final long earliestStartTime, final long latestStartTime, final int first, final int max, final boolean mostRecentFirst)
    {
        return queryBuilds(projects, states, null, earliestStartTime, latestStartTime, first, max, mostRecentFirst, true);
    }

    public List<BuildResult> queryBuilds(final Project[] projects, final ResultState[] states, final String[] statuses, final long earliestStartTime, final long latestStartTime, final int first, final int max, final boolean mostRecentFirst, final boolean includePinned)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Restrictions.isNull("user"));
                if (!includePinned)
                {
                    criteria.add(Restrictions.eq("pinned", false));
                }
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
        List<BuildResult> results = getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Restrictions.isNull("user"));
                criteria.add(Restrictions.eq("project", project));
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
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Restrictions.isNull("user"));
                addProjectsToCriteria(projects, criteria);
                criteria.add(Restrictions.gt(level.toString().toLowerCase() + "FeatureCount", 0));

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
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(BuildResult.class);
                criteria.add(Restrictions.eq("user", user));

                if (states != null)
                {
                    criteria.add(Restrictions.in("stateName", getStateNames(states)));
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
        return toInt(getHibernateTemplate().execute(new HibernateCallback<Long>()
        {
            public Long doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("select count (*) from BuildResult model where model.user = :user and model.stateName in (:stateNames)");
                queryObject.setEntity("user", user);
                queryObject.setParameterList("stateNames", getStateNames(ResultState.getCompletedStates()));
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return (Long) queryObject.uniqueResult();
            }
        }));
    }

    public List<BuildResult> getOldestCompletedBuilds(final User user, final int offset)
    {
        int count = getCompletedResultCount(user);
        if (count > offset)
        {
            final int max = count - offset;

            return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
            {
                public List<BuildResult> doInHibernate(Session session) throws HibernateException
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

    public RecipeResultNode findResultNodeByResultId(long id)
    {
        return (RecipeResultNode) findUniqueByNamedQuery("findResultNodeByResultId", "id", id, true);
    }

    public BuildResult findLatest(final ResultState... inStates)
    {
        return findLatestByProject(null, false, inStates);
    }

    public CommandResult findCommandResultByArtifact(final long artifactId)
    {
        return getHibernateTemplate().execute(new HibernateCallback<CommandResult>()
        {
            public CommandResult doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("SELECT result " +
                        "FROM CommandResult result " +
                        "JOIN result.artifacts artifact " +
                        "WHERE artifact.id = :id");
                queryObject.setLong("id", artifactId);
                queryObject.setMaxResults(1);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return (CommandResult) queryObject.uniqueResult();
            }
        });
    }

    public BuildResult findLatestByProject(final Project project, final boolean initialise, final ResultState... inStates)
    {
        BuildResult buildResult = getHibernateTemplate().execute(new HibernateCallback<BuildResult>()
        {
            public BuildResult doInHibernate(Session session) throws HibernateException {
                Criteria criteria = getBuildResultCriteria(session, project, inStates, false);
                criteria.setMaxResults(1);
                criteria.addOrder(Order.desc("id"));
                return (BuildResult) criteria.uniqueResult();
            }
        });

        if (initialise)
        {
            initialise(buildResult);
        }

        return buildResult;
    }

    public BuildResult findByRecipeId(long id)
    {
        final RecipeResultNode node = findResultNodeByResultId(id);
        if (node == null)
        {
            return null;
        }

        return getHibernateTemplate().execute(new HibernateCallback<BuildResult>()
        {
            public BuildResult doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("select result from BuildResult result join result.stages stage where stage = :node");
                queryObject.setEntity("node", node);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return (BuildResult) queryObject.uniqueResult();
            }
        });
    }

    public int getBuildCount(final String agent, final ResultState[] states)
    {
        Long count = getHibernateTemplate().execute(new HibernateCallback<Long>()
        {
            public Long doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery(
                        "select count(distinct result) from BuildResult result " +
                                "  join result.stages stage " +
                                "where stage.agentName = :agent" +
                                (states == null ? "" : " and result.stateName in (:states)"));

                queryObject.setString("agent", agent);
                if (states != null)
                {
                    queryObject.setParameterList("states", getStateNames(states));
                }

                return (Long) queryObject.uniqueResult();
            }
        });
        
        return count.intValue();
    }

    public List<BuildResult> findLatestByAgentName(final String agent, final ResultState[] states, final int first, final int max)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery(
                        "select distinct result from BuildResult result " +
                        "  join result.stages stage " +
                        "where stage.agentName = :agent " +
                        (states == null ? "" : "and result.stateName in (:states) ") +
                        "order by result.id desc");

                queryObject.setString("agent", agent);
                if (states != null)
                {
                    queryObject.setParameterList("states", getStateNames(states));
                }

                if (first >= 0)
                {
                    queryObject.setFirstResult(first);
                }
                
                if (max >= 0)
                {
                    queryObject.setMaxResults(max);
                }

                return queryObject.list();
            }
        });
    }

    public List<BuildResult> findByBeforeBuild(final long buildId, final int maxResults, final ResultState... states)
    {
        final BuildResult result = findById(buildId);
        List<BuildResult> results = getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, result.getProject(), states, result.isPersonal());
                criteria.add(Restrictions.lt("id", buildId));
                if (result.isPersonal())
                {
                    criteria.add(Restrictions.eq("user", result.getUser()));
                }
                criteria.addOrder(Order.desc("id"));
                criteria.setMaxResults(maxResults);
                SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
                return criteria.list();
            }
        });
        return CollectionUtils.reverse(results);
    }

    public List<BuildResult> findByAfterBuild(final long buildId, final int maxResults, final ResultState... states)
    {
        final BuildResult result = findById(buildId);
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildResult>>()
        {
            public List<BuildResult> doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, result.getProject(), states, result.isPersonal());
                criteria.add(Restrictions.gt("id", buildId));
                if (result.isPersonal())
                {
                    criteria.add(Restrictions.eq("user", result.getUser()));
                }
                criteria.addOrder(Order.asc("id"));
                criteria.setMaxResults(maxResults);
                SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
                return criteria.list();
            }
        });
    }

    public BuildResult findByLatestBuild(final long buildId, final ResultState... states)
    {
        final BuildResult result = findById(buildId);
        return getHibernateTemplate().execute(new HibernateCallback<BuildResult>()
        {
            public BuildResult doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = getBuildResultCriteria(session, result.getProject(), states, result.isPersonal());
                if (result.isPersonal())
                {
                    criteria.add(Restrictions.eq("user", result.getUser()));
                }
                criteria.addOrder(Order.desc("id"));
                criteria.setMaxResults(1);
                SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
                return (BuildResult) criteria.uniqueResult();
            }
        });
    }

    public static void initialise(final BuildResult result)
    {
        if (result != null)
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
    }

    private Criteria getBuildResultCriteria(Session session, Project project, ResultState[] states, boolean includePersonal)
    {
        Criteria criteria = session.createCriteria(BuildResult.class);
        if (!includePersonal)
        {
            criteria.add(Restrictions.isNull("user"));
        }

        if (project != null)
        {
            criteria.add(Restrictions.eq("project", project));
        }

        addStatesToCriteria(states, criteria);

        SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
        return criteria;
    }

    private void addProjectsToCriteria(Project[] projects, Criteria criteria)
    {
        if (projects != null && projects.length > 0)
        {
            criteria.add(Restrictions.in("project", projects));
        }
    }

    private void addStatesToCriteria(ResultState[] states, Criteria criteria)
    {
        if (states != null && states.length > 0)
        {
            criteria.add(Restrictions.in("stateName", getStateNames(states)));
        }
    }

    private void addStatusesToCriteria(String[] statuses, Criteria criteria)
    {
        if (statuses != null && statuses.length > 0)
        {
            criteria.add(Restrictions.in("status", statuses));
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
            criteria.add(Restrictions.ge("stamps.startTime", earliestStartTime));
        }

        if (latestStartTime > 0)
        {
            // CIB-446: Don't accept timestamps that are uninitialised
            criteria.add(Restrictions.ge("stamps.startTime", 0L));
            criteria.add(Restrictions.le("stamps.startTime", latestStartTime));
        }
    }

    private void addNumbersToCriteria(long lowestNumber, long highestNumber, Criteria criteria)
    {
        if (lowestNumber > 0)
        {
            criteria.add(Restrictions.ge("number", lowestNumber));
        }

        if (highestNumber > 0)
        {
            criteria.add(Restrictions.le("number", highestNumber));
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
