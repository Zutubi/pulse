package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.prototype.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.model.ChangelistUtils;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.ChangelistDao;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.scm.config.ScmConfiguration;
import com.zutubi.pulse.web.ActionSupport;

import java.util.List;

/**
 */
public class ViewChangelistAction extends ActionSupport
{
    private long id;
    private Changelist changelist;
    private ChangelistDao changelistDao;
    private BuildManager buildManager;

    /** If we drilled down from the project, this is the project ID */
    private long projectId;
    private Project project;

    /** This is the build result we have drilled down from, if any. */
    private BuildResult buildResult;
    private long buildId;

    /** All builds affected by this change. */
    private List<BuildResult> buildResults;

    private boolean changeViewerInitialised;
    private ChangeViewerConfiguration changeViewer;
    private ScmConfiguration scm;

    private String fileViewUrl;
    private String fileDownloadUrl;
    private String fileDiffUrl;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return project;
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

    public ChangeViewerConfiguration getChangeViewer()
    {
        if(!changeViewerInitialised)
        {
            changeViewerInitialised = true;
            
            Project p = project;
            if(buildResult != null)
            {
                p = buildResult.getProject();
            }

            if(p != null)
            {
                ProjectConfiguration projectConfig = projectManager.getProjectConfig(p.getId());
                changeViewer = projectConfig.getChangeViewer();
                scm = projectConfig.getScm();
            }
            else
            {
                for(long id: changelist.getProjectIds())
                {
                    p = projectManager.getProject(id);
                    ProjectConfiguration projectConfig = projectManager.getProjectConfig(id);
                    if(p != null && projectConfig != null && projectConfig.getChangeViewer() != null)
                    {
                        changeViewer = projectConfig.getChangeViewer();
                        scm = projectConfig.getScm();
                        break;
                    }
                }
            }
        }

        return changeViewer;
    }

    public boolean haveChangeViewer()
    {
        return getChangeViewer() != null;
    }
    
    public String getChangeUrl()
    {
        ChangeViewerConfiguration changeViewer = getChangeViewer();
        if(changeViewer != null && changeViewer.hasCapability(scm, ChangeViewerConfiguration.Capability.VIEW_CHANGESET))
        {
            return changeViewer.getChangesetURL(changelist.getRevision());
        }

        return null;
    }

    public String getFileViewUrl()
    {
        return fileViewUrl;
    }

    public String getFileDownloadUrl()
    {
        return fileDownloadUrl;
    }

    public String getFileDiffUrl()
    {
        return fileDiffUrl;
    }

    public void updateUrls(Change change)
    {
        updateFileViewUrl(change);
        updateFileDownloadUrl(change);
        updateFileDiffUrl(change);
    }

    public void updateFileViewUrl(Change change)
    {
        ChangeViewerConfiguration changeViewer = getChangeViewer();
        if(changeViewer != null && changeViewer.hasCapability(scm, ChangeViewerConfiguration.Capability.VIEW_FILE))
        {
            fileViewUrl = changeViewer.getFileViewURL(change.getFilename(), change.getRevision());
        }
        else
        {
            fileViewUrl = null;
        }
    }

    public void updateFileDownloadUrl(Change change)
    {
        ChangeViewerConfiguration changeViewer = getChangeViewer();
        if(changeViewer != null && changeViewer.hasCapability(scm, ChangeViewerConfiguration.Capability.DOWNLOAD_FILE))
        {
            fileDownloadUrl = changeViewer.getFileDownloadURL(change.getFilename(), change.getRevision());
        }
        else
        {
            fileDownloadUrl = null;
        }
    }

    public void updateFileDiffUrl(Change change)
    {
        if(diffableAction(change.getAction()))
        {
            FileRevision previous = change.getRevision().getPrevious();
            if(previous != null)
            {
                ChangeViewerConfiguration changeViewer = getChangeViewer();
                if(changeViewer != null && changeViewer.hasCapability(scm, ChangeViewerConfiguration.Capability.VIEW_FILE_DIFF))
                {
                    fileDiffUrl = changeViewer.getFileDiffURL(change.getFilename(), change.getRevision());
                    return;
                }
            }
        }

        fileDiffUrl = null;
    }

    private boolean diffableAction(Change.Action action)
    {
        switch(action)
        {
            case EDIT:
            case INTEGRATE:
            case MERGE:
                return true;
            default:
                return false;
        }
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
        if(projectId != 0)
        {
            project = projectManager.getProject(projectId);    
        }

        if(buildId != 0)
        {
            // It is valid to have no build ID set: we may not be viewing
            // the change as part of a build.
            buildResult = buildManager.getBuildResult(buildId);
        }

        buildResults = ChangelistUtils.getBuilds(buildManager, changelist);
/*
        Collections.sort(buildResults, new Comparator<BuildResult>()
        {
            public int compare(BuildResult b1, BuildResult b2)
            {
                NamedEntityComparator comparator = new NamedEntityComparator();
                int result = comparator.compare(b1.getProject(), b2.getProject());
                if(result == 0)
                {
                    result = (int)(b1.getNumber() - b2.getNumber());
                }

                return result;
            }
        });
*/
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
