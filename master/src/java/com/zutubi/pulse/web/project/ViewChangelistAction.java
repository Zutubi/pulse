/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.ChangelistUtils;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.model.persistence.ChangelistDao;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.ProjectNameComparator;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 */
public class ViewChangelistAction extends ActionSupport
{
    private long id;
    private Changelist changelist;
    private ChangelistDao changelistDao;
    private BuildManager buildManager;
    private long buildId;
    /** This is the build result we have drilled down from, if any. */
    private BuildResult buildResult;
    /** All builds affected by this change. */
    private List<BuildResult> buildResults;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public Changelist getChangelist()
    {
        return changelist;
    }

    public void setChangelist(Changelist changelist)
    {
        this.changelist = changelist;
    }

    public void validate()
    {
        changelist = changelistDao.findById(id);
        if (changelist == null)
        {
            addActionError("Unknown changelist '" + id + "'");
        }
    }

    public String execute()
    {
        if(buildId != 0)
        {
            // It is valid to have no build ID set: we may not be viewing
            // the change as part of a build.
            buildResult = buildManager.getBuildResult(buildId);
        }

        buildResults = ChangelistUtils.getBuilds(buildManager, changelist);
        Collections.sort(buildResults, new Comparator<BuildResult>()
        {
            public int compare(BuildResult b1, BuildResult b2)
            {
                ProjectNameComparator comparator = new ProjectNameComparator();
                int result = comparator.compare(b1.getProject(), b2.getProject());
                if(result == 0)
                {
                    result = (int)(b1.getNumber() - b2.getNumber());
                }

                return result;
            }
        });
        
        // TODO dodgy walking of tree: hibernate eager/lazy loading!
        changelist.getRevision();
        changelist.getChanges();
        return SUCCESS;
    }

    public void setChangelistDao(ChangelistDao changelistDao)
    {
        this.changelistDao = changelistDao;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public List<BuildResult> getBuildResults()
    {
        return buildResults;
    }
}
