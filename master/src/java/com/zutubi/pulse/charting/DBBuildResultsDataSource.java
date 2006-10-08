package com.zutubi.pulse.charting;

import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.BuildResult;

import java.util.List;
import java.util.Calendar;

/**
 * <class comment/>
 */
public class DBBuildResultsDataSource implements BuildResultsDataSource
{
    private BuildResultDao buildResultDao;

    private Project project;

    public void setProject(Project project)
    {
        this.project = project;
    }

    public BuildResultsResultSet getLastByBuilds(int builds)
    {
        List<BuildResult> results = buildResultDao.findLatestByProject(project, builds);
        return new BuildResultsResultSet(results);
    }

    public BuildResultsResultSet getLastByDays(int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        List<BuildResult> results = buildResultDao.findSinceByProject(project, cal.getTime());
        return new BuildResultsResultSet(results);
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }
}
