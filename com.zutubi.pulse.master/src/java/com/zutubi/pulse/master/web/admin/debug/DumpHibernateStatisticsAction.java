package com.zutubi.pulse.master.web.admin.debug;

import com.zutubi.pulse.master.web.ActionSupport;
import org.hibernate.SessionFactory;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;

import java.util.Map;
import java.util.TreeMap;

/**
 */
public class DumpHibernateStatisticsAction extends ActionSupport
{
    private SessionFactory sessionFactory;
    private boolean on;
    private Statistics stats;
    private long secondLevelCacheSize = 0;
    private Map<String, SecondLevelCacheStatistics> secondLevelCacheStats = new TreeMap<String, SecondLevelCacheStatistics>();
    private Map<String, QueryStatistics> queryStats = new TreeMap<String, QueryStatistics>();
    private Map<String, EntityStatistics> entityStats = new TreeMap<String, EntityStatistics>();

    public boolean isOn()
    {
        return on;
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
        }

        return SUCCESS;
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
