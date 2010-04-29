package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeContext;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeContextImpl;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.PagingSupport;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.AccessDeniedException;

import java.util.*;

/**
 * Action to display information about a single changelist.  This action does
 * not fit cleanly into our hierarchy as it can be accessed from multiple
 * locations, even for a single changelist.
 */
public class ViewChangelistAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(ViewChangelistAction.class);

    /**
     * The maximum number of files to list per page.
     */
    private static final int FILES_PER_PAGE = 100;

    private long id;
    private PagingSupport pagingSupport = new PagingSupport(FILES_PER_PAGE);
    private PersistentChangelist changelist;
    private List<PersistentFileChange> files;
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

    private boolean changeViewerFound;
    private String changeUrl;
    private List<FileModel> fileModels = new LinkedList<FileModel>();

    private ScmManager scmManager;

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

    public List<BuildResult> getBuildResults()
    {
        return buildResults;
    }

    public boolean isChangeViewerFound()
    {
        return changeViewerFound;
    }

    public String getChangeUrl()
    {
        return changeUrl;
    }

    public BuildManager getBuildManager()
    {
        return buildManager;
    }

    public List<FileModel> getFileModels()
    {
        return fileModels;
    }

    public PagingSupport getPagingSupport()
    {
        return pagingSupport;
    }

    public void setStartPage(int startPage)
    {
        pagingSupport.setStartPage(startPage);
    }

    public String execute()
    {
        changelist = changelistDao.findById(id);
        if (changelist == null)
        {
            addActionError("Unknown changelist '" + id + "'");
            return ERROR;
        }

        pagingSupport.setTotalItems(changelistDao.getSize(changelist));
        files = changelistDao.getFiles(changelist, pagingSupport.getStartOffset(), FILES_PER_PAGE);
        
        if (StringUtils.stringSet(projectName))
        {
            project = projectManager.getProject(projectName, false);
        }
        if (StringUtils.stringSet(buildVID))
        {
            // It is valid to have no build ID set: we may not be viewing
            // the change as part of a build.
            buildResult = buildManager.getByProjectAndVirtualId(project, buildVID);
        }

        Set<Long> buildIds = changelistDao.getAllAffectedResultIds(changelist);
        buildResults = new LinkedList<BuildResult>();
        for (Long id : buildIds)
        {
            try
            {
                buildResults.add(buildManager.getBuildResult(id));
            }
            catch (AccessDeniedException e)
            {
                // User can't view this one, just continue.
            }
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

        ProjectConfiguration projectWithViewer = getProjectWithChangeViewer();
        if (projectWithViewer == null)
        {
            // No need to create ChangeContext and all which that entails.
            createSimpleFileModels();
        }
        else
        {
            createLinkedFileModels(projectWithViewer);
        }

        return SUCCESS;
    }

    private ProjectConfiguration getProjectWithChangeViewer()
    {
        Project p = project;
        if (buildResult != null)
        {
            p = buildResult.getProject();
        }

        if (hasChangeViewer(p))
        {
            return p.getConfig();
        }

        for (long id : changelistDao.getAllAffectedProjectIds(changelist))
        {
            try
            {
                p = projectManager.getProject(id, false);
                if (hasChangeViewer(p))
                {
                    return p.getConfig();
                }
            }
            catch (AccessDeniedException e)
            {
                // User can't view this project, just continue.
            }
        }

        return null;
    }

    private boolean hasChangeViewer(Project p)
    {
        if (p != null)
        {
            ProjectConfiguration projectConfig = p.getConfig();
            return projectConfig != null && projectConfig.getChangeViewer() != null && projectConfig.getScm() != null;
        }
        return false;
    }

    private void createSimpleFileModels()
    {
        CollectionUtils.map(files, new Mapping<PersistentFileChange, FileModel>()
        {
            public FileModel map(PersistentFileChange persistentFileChange)
            {
                return new FileModel(persistentFileChange.asChange());
            }
        }, fileModels);
    }

    private void createLinkedFileModels(ProjectConfiguration projectWithViewer)
    {
        ScmConfiguration scmConfiguration = projectWithViewer.getScm();
        final ChangeViewerConfiguration changeViewer = projectWithViewer.getChangeViewer();
        ScmClient scmClient = null;
        try
        {
            ScmContext scmContext = scmManager.createContext(projectWithViewer);
            scmClient = scmManager.createClient(scmConfiguration);
            final ChangeContext context = new ChangeContextImpl(changelist.getRevision(), scmConfiguration, scmClient, scmContext);

            changeViewerFound = true;
            changeUrl = getChangeUrl(changeViewer, changelist.getRevision());

            CollectionUtils.map(files, new Mapping<PersistentFileChange, FileModel>()
            {
                public FileModel map(PersistentFileChange persistentFileChange)
                {
                    FileChange change = persistentFileChange.asChange();
                    try
                    {
                        return new FileModel(
                                persistentFileChange.asChange(),
                                getFileViewUrl(changeViewer, context, change),
                                getFileDownloadUrl(changeViewer, context, change),
                                getFileDiffUrl(changeViewer, context, change)
                        );
                    }
                    catch (ScmException e)
                    {
                        LOG.warning(e);
                        return new FileModel(persistentFileChange.asChange());
                    }
                }
            }, fileModels);
        }
        catch (ScmException e)
        {
            LOG.warning(e);

            // Fall back on a page with no change viewer links.
            changeViewerFound = false;
            fileModels.clear();
            createSimpleFileModels();
        }
        finally
        {
            IOUtils.close(scmClient);
        }
    }

    private String getChangeUrl(ChangeViewerConfiguration changeViewer, Revision revision)
    {
        if (changeViewer.hasCapability(ChangeViewerConfiguration.Capability.VIEW_REVISION))
        {
            return changeViewer.getRevisionURL(revision);
        }

        return null;
    }

    private String getFileViewUrl(ChangeViewerConfiguration changeViewer, ChangeContext context, FileChange change) throws ScmException
    {
        if (changeViewer.hasCapability(ChangeViewerConfiguration.Capability.VIEW_FILE))
        {
            return changeViewer.getFileViewURL(context, change);
        }
        else
        {
            return null;
        }
    }

    public String getFileDownloadUrl(ChangeViewerConfiguration changeViewer, ChangeContext context, FileChange change) throws ScmException
    {
        if (changeViewer.hasCapability(ChangeViewerConfiguration.Capability.DOWNLOAD_FILE))
        {
            return changeViewer.getFileDownloadURL(context, change);
        }
        else
        {
            return null;
        }
    }

    public String getFileDiffUrl(ChangeViewerConfiguration changeViewer, ChangeContext context, FileChange change) throws ScmException
    {
        if (changeViewer.hasCapability(ChangeViewerConfiguration.Capability.VIEW_FILE_DIFF) && diffableAction(change.getAction()))
        {
            return changeViewer.getFileDiffURL(context, change);
        }
        else
        {
            return null;
        }
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

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public static class FileModel
    {
        private FileChange fileChange;
        private String viewUrl;
        private String downloadUrl;
        private String diffUrl;

        public FileModel(FileChange fileChange)
        {
            this.fileChange = fileChange;
        }

        public FileModel(FileChange fileChange, String viewUrl, String downloadUrl, String diffUrl)
        {
            this.fileChange = fileChange;
            this.viewUrl = viewUrl;
            this.downloadUrl = downloadUrl;
            this.diffUrl = diffUrl;
        }

        public String getPath()
        {
            return fileChange.getPath();
        }

        public Revision getRevision()
        {
            return fileChange.getRevision();
        }

        public FileChange.Action getAction()
        {
            return fileChange.getAction();
        }

        public String getViewUrl()
        {
            return viewUrl;
        }

        public String getDownloadUrl()
        {
            return downloadUrl;
        }

        public String getDiffUrl()
        {
            return diffUrl;
        }
    }
}
