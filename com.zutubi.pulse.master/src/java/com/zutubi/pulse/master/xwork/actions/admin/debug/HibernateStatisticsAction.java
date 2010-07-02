package com.zutubi.pulse.master.xwork.actions.admin.debug;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Sort;
import org.hibernate.SessionFactory;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;

import java.util.*;

/**
 * The hibernate statistics action provides access to the various
 * statistics gathered by the hibernate session factory.
 *
 * By default, statistics gathering is disabled.  It can be disabled
 * via the {@link #toggle()} action.
 *
 * @see org.hibernate.SessionFactory#getStatistics() 
 */
public class HibernateStatisticsAction extends ActionSupport
{
    private SessionFactory sessionFactory;
    private boolean on;
    private Statistics stats;
    private long secondLevelCacheSize = 0;

    private Map<String, SecondLevelCacheStatistics> secondLevelCacheStats = new TreeMap<String, SecondLevelCacheStatistics>();
    private Map<String, QueryStatistics> queryStats = new TreeMap<String, QueryStatistics>();
    private Map<String, EntityStatistics> entityStats = new TreeMap<String, EntityStatistics>();

    private List<String> entityNames = new LinkedList<String>();

    public boolean isOn()
    {
        return on;
    }

    public void setOn(boolean on)
    {
        this.on = on;
    }

    public int getRandom()
    {
        return (int) (Math.random() * 4096);
    }

    public Statistics getStats()
    {
        return stats;
    }

    public long getSecondLevelCacheSize()
    {
        return secondLevelCacheSize;
    }

    public String getSecondLevelCacheSizeKB()
    {
        return String.format("%.02f", secondLevelCacheSize / 1024.0);
    }

    public Map<String, QueryStatistics> getQueryStats()
    {
        return queryStats;
    }

    public Map<String, EntityStatistics> getEntityStats()
    {
        return entityStats;
    }

    public List<String> getEntityNames()
    {
        return entityNames;
    }

    public String getSecondLevelCacheSizeMB()
    {
        return String.format("%.02f", secondLevelCacheSize / (1024.0 * 1024.0));
    }

    public Map<String, SecondLevelCacheStatistics> getSecondLevelCacheStats()
    {
        return secondLevelCacheStats;
    }

    public String execute() throws Exception
    {
        loadStatistics();

        return SUCCESS;
    }

    public String toggle() throws Exception
    {
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(!stats.isStatisticsEnabled());

        return "toggled";
    }

    private void loadStatistics()
    {
        stats = sessionFactory.getStatistics();
        on = stats.isStatisticsEnabled();

        if (on)
        {
            for (String region : stats.getSecondLevelCacheRegionNames())
            {
                SecondLevelCacheStatistics regionStats = stats.getSecondLevelCacheStatistics(region);
                secondLevelCacheSize += regionStats.getSizeInMemory();
                secondLevelCacheStats.put(region, regionStats);
            }

            for(String query: stats.getQueries())
            {
                queryStats.put(query, stats.getQueryStatistics(query));
            }

            for (String entity : stats.getEntityNames())
            {
                entityStats.put(entity, stats.getEntityStatistics(entity));
            }

            entityNames.addAll(entityStats.keySet());
            Collections.sort(entityNames, new Sort.StringComparator());
        }
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
