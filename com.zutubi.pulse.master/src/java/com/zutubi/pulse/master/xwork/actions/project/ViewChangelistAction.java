package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.util.Sort;
import com.zutubi.util.TextUtils;

import java.util.*;

/**
 */
public class ViewChangelistAction extends ActionSupport
{
    private long id;
    private PersistentChangelist changelist;
    private BuildManager buildManager;

    /**
     * If we drilled down from the project, this is the project ID
     */
    private String projectName;
    private Project project;

    /**
     * This is the build result we have drilled down from, if any.
     */
    private String buildVID;
    private BuildResult buildResult;

    /**
     * All builds affected by this change.
     */
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

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getu_projectName()
    {
        return uriComponentEncode(projectName);
    }

    public String geth_projectName()
    {
        return htmlEncode(projectName);
    }

    public Project getProject()
    {
        return project;
    }

    public String getBuildVID()
    {
        return buildVID;
    }

    public void setBuildVID(String buildVID)
    {
        this.buildVID = buildVID;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public PersistentChangelist getChangelist()
    {
        return changelist;
    }

    public ChangeViewerConfiguration getChangeViewer()
    {
        if (!changeViewerInitialised)
        {
            changeViewerInitialised = true;

            Project p = project;
            if (buildResult != null)
            {
                p = buildResult.getProject();
            }

            if (!getViewerFromProject(p))
            {
                for (long id : changelistDao.getAllAffectedProjectIds(changelist))
                {
                    p = projectManager.getProject(id, false);
                    if (getViewerFromProject(p))
                    {
                        break;
                    }
                }
            }
        }

        return changeViewer;
    }

    private boolean getViewerFromProject(Project p)
    {
        if (p != null)
        {
            ProjectConfiguration projectConfig = p.getConfig();
            if (projectConfig != null && projectConfig.getChangeViewer() != null && projectConfig.getScm() != null)
            {
                changeViewer = projectConfig.getChangeViewer();
                scm = projectConfig.getScm();
                return true;
            }
        }
        return false;
    }

    public boolean haveChangeViewer()
    {
        return getChangeViewer() != null;
    }

    public String getChangeUrl()
    {
        ChangeViewerConfiguration changeViewer = getChangeViewer();
        if (changeViewer != null && changeViewer.hasCapability(ChangeViewerConfiguration.Capability.VIEW_CHANGELIST))
        {
            return changeViewer.getChangelistURL(changelist.getRevision());
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

    public void updateUrls(PersistentChangelist changelist, PersistentFileChange change)
    {
        updateFileViewUrl(changelist, change);
        updateFileDownloadUrl(changelist, change);
        updateFileDiffUrl(changelist, change);
    }

    public void updateFileViewUrl(PersistentChangelist changelist, PersistentFileChange change)
    {
        ChangeViewerConfiguration changeViewer = getChangeViewer();
        if (changeViewer != null && changeViewer.hasCapability(ChangeViewerConfiguration.Capability.VIEW_FILE))
        {
            fileViewUrl = changeViewer.getFileViewURL(change.getFilename(), changelist.getRevision(), change.getRevisionString());
        }
        else
        {
            fileViewUrl = null;
        }
    }

    public void updateFileDownloadUrl(PersistentChangelist changelist, PersistentFileChange change)
    {
        ChangeViewerConfiguration changeViewer = getChangeViewer();
        if (changeViewer != null && changeViewer.hasCapability(ChangeViewerConfiguration.Capability.DOWNLOAD_FILE))
        {
            fileDownloadUrl = changeViewer.getFileDownloadURL(change.getFilename(), changelist.getRevision(), change.getRevisionString());
        }
        else
        {
            fileDownloadUrl = null;
        }
    }

    public void updateFileDiffUrl(PersistentChangelist changelist, PersistentFileChange change)
    {
        ChangeViewerConfiguration changeViewer = getChangeViewer();
        if (changeViewer != null && changeViewer.hasCapability(ChangeViewerConfiguration.Capability.VIEW_FILE_DIFF))
        {
            if (diffableAction(change.asChange().getAction()))
            {
                String previous = scm.getPreviousRevision(change.getRevisionString());
                if (previous != null)
                {
                    fileDiffUrl = changeViewer.getFileDiffURL(change.getFilename(), changelist.getRevision(), change.getRevisionString());
                    return;
                }
            }
        }

        fileDiffUrl = null;
    }

    private boolean diffableAction(FileChange.Action action)
    {
        switch (action)
        {
            case EDIT:
            case INTEGRATE:
            case MERGE:
                return true;
            default:
                return false;
        }
    }

    public void setChangelist(PersistentChangelist changelist)
    {
        this.changelist = changelist;
    }

    public String execute()
    {
        changelist = changelistDao.findById(id);
        if (changelist == null)
        {
            addActionError("Unknown changelist '" + id + "'");
            return ERROR;
        }

        if (TextUtils.stringSet(projectName))
        {
            project = projectManager.getProject(projectName, false);
        }
        if (TextUtils.stringSet(buildVID))
        {
            // It is valid to have no build ID set: we may not be viewing
            // the change as part of a build.
            buildResult = buildManager.getByProjectAndVirtualId(project, buildVID);
        }

        Set<Long> buildIds = changelistDao.getAllAffectedResultIds(changelist);
        buildResults = new LinkedList<BuildResult>();
        for(Long id: buildIds)
        {
            buildResults.add(buildManager.getBuildResult(id));
        }

        Collections.sort(buildResults, new Comparator<BuildResult>()
        {
            public int compare(BuildResult b1, BuildResult b2)
            {
                Sort.StringComparator comparator = new Sort.StringComparator();
                int result = comparator.compare(b1.getProject().getName(), b2.getProject().getName());
                if (result == 0)
                {
                    result = (int) (b1.getNumber() - b2.getNumber());
                }

                return result;
            }
        });

        return SUCCESS;
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
