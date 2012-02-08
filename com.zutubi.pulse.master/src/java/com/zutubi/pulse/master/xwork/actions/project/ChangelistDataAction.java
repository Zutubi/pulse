package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeContext;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeContextImpl;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.EnumUtils;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

/**
 * Assembles JSON data for the changelist panel.
 */
public class ChangelistDataAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(ChangelistDataAction.class);
    
    /**
     * The maximum number of files to list per page.
     */
    private static final int FILES_PER_PAGE = 100;

    private int id;
    private int startPage;
    private String projectName;
    private ChangelistViewModel model;

    private BuildManager buildManager;
    private MasterConfigurationManager configurationManager;
    private ScmManager scmManager;

    public void setId(int id)
    {
        this.id = id;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public ChangelistViewModel getModel()
    {
        return model;
    }

    public String execute()
    {
        PersistentChangelist changelist = changelistDao.findById(id);
        if (changelist == null)
        {
            throw new LookupErrorException("Unknown changelist [" + id + "]");
        }

        int fileCount = changelistDao.getSize(changelist);
        int pageCount = (fileCount + FILES_PER_PAGE - 1) / FILES_PER_PAGE;
        if (startPage < 0)
        {
            startPage = 0;
        }

        if (startPage >= pageCount)
        {
            startPage = pageCount - 1;
        }

        // If we are within a project or build, our context implies a specific project.
        // We use this project for the change viewer and commit message transformers.
        // If we are not in such context (e.g. we are coming from the dashboard), we
        // later look for any project affected by this change with a change viewer.
        Project contextProject = null;
        Project projectWithChangeViewer = null;
        if (StringUtils.stringSet(projectName))
        {
            contextProject = projectManager.getProject(projectName, false);
            if (contextProject != null && contextProject.getConfig().getChangeViewer() != null)
            {
                projectWithChangeViewer = contextProject;
            }
        }

        Set<Long> buildIds = changelistDao.getAllAffectedResultIds(changelist);
        List<BuildResult> buildResults = new LinkedList<BuildResult>();
        for (Long id : buildIds)
        {
            try
            {
                BuildResult buildResult = buildManager.getBuildResult(id);
                buildResults.add(buildResult);
                if (contextProject == null && projectWithChangeViewer == null &&
                    buildResult.getProject().getConfig().getChangeViewer() != null)
                {
                    projectWithChangeViewer = buildResult.getProject();
                }
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

        ChangeViewerConfiguration changeViewer = projectWithChangeViewer == null ? null : projectWithChangeViewer.getConfig().getChangeViewer();
        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        List<BuildModel> builds = CollectionUtils.map(buildResults, new BuildResultToModelMapping(urls));

        Collection<CommitMessageTransformerConfiguration> transformers;
        if(contextProject == null)
        {
            transformers = Collections.emptyList();
        }
        else
        {
            transformers = contextProject.getConfig().getCommitMessageTransformers().values();
        }

        ChangelistModel changelistModel = new ChangelistModel(changelist, changeViewer, transformers);
        model = new ChangelistViewModel(changelistModel, builds, new PagerModel(fileCount, FILES_PER_PAGE, startPage));

        List<PersistentFileChange> files = changelistDao.getFiles(changelist, startPage * FILES_PER_PAGE, FILES_PER_PAGE);
        if (projectWithChangeViewer == null)
        {
            // No need to create ChangeContext and all which that entails.
            addSimpleFileModels(files);
        }
        else
        {
            addLinkedFileModels(changelist.getRevision(), files, projectWithChangeViewer);
        }

        return SUCCESS;
    }

    private void addSimpleFileModels(List<PersistentFileChange> files)
    {
        for (PersistentFileChange file: files)
        {
            model.addFile(new ChangelistFileModel(file.asChange()));
        }
    }

    private void addLinkedFileModels(Revision revision, List<PersistentFileChange> files, Project projectWithViewer)
    {
        ProjectConfiguration projectConfiguration = projectWithViewer.getConfig();
        ScmConfiguration scmConfiguration = projectConfiguration.getScm();
        final ChangeViewerConfiguration changeViewer = projectConfiguration.getChangeViewer();
        ScmClient scmClient = null;
        try
        {
            scmClient = scmManager.createClient(scmConfiguration);
            ScmContext scmContext = scmManager.createContext(projectWithViewer.getConfig(), projectWithViewer.getState(), scmClient.getImplicitResource());
            final ChangeContext context = new ChangeContextImpl(revision, scmConfiguration, scmClient, scmContext);

            for (PersistentFileChange file: files)
            {
                FileChange change = file.asChange();
                ChangelistFileModel fileModel = new ChangelistFileModel(change);
                model.addFile(fileModel);
                try
                {
                    fileModel.getLinks().setViewUrl(getFileViewUrl(changeViewer, context, change));
                    fileModel.getLinks().setDownloadUrl(getFileDownloadUrl(changeViewer, context, change));
                    fileModel.getLinks().setDiffUrl(getFileDiffUrl(changeViewer, context, change));
                }
                catch (ScmException e)
                {
                    LOG.warning(e);
                }
            }
        }
        catch (ScmException e)
        {
            LOG.warning(e);
            addSimpleFileModels(files);
        }
        finally
        {
            IOUtils.close(scmClient);
        }
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

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public static class ChangelistViewModel
    {
        private ChangelistModel changelist;
        private List<BuildModel> builds;
        private List<ChangelistFileModel> files = new LinkedList<ChangelistFileModel>();
        private PagerModel pager;

        public ChangelistViewModel(ChangelistModel changelist, List<BuildModel> builds, PagerModel pager)
        {
            this.changelist = changelist;
            this.builds = builds;
            this.pager = pager;
        }

        public ChangelistModel getChangelist()
        {
            return changelist;
        }

        public List<BuildModel> getBuilds()
        {
            return builds;
        }

        public List<ChangelistFileModel> getFiles()
        {
            return files;
        }

        public void addFile(ChangelistFileModel file)
        {
            files.add(file);
        }

        public PagerModel getPager()
        {
            return pager;
        }
    }

    public static class ChangelistFileModel
    {
        private String fileName;
        private String revision;
        private String action;
        private FileLinksModel links = new FileLinksModel();

        public ChangelistFileModel(FileChange change)
        {
            fileName = change.getPath();
            revision = change.getRevision().getRevisionString();
            action = EnumUtils.toPrettyString(change.getAction());
        }

        public String getFileName()
        {
            return fileName;
        }

        public String getRevision()
        {
            return revision;
        }

        public String getAction()
        {
            return action;
        }

        public FileLinksModel getLinks()
        {
            return links;
        }
    }

    public static class FileLinksModel
    {
        private String viewUrl;
        private String downloadUrl;
        private String diffUrl;

        public String getViewUrl()
        {
            return viewUrl;
        }

        public void setViewUrl(String viewUrl)
        {
            this.viewUrl = viewUrl;
        }

        public String getDownloadUrl()
        {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl)
        {
            this.downloadUrl = downloadUrl;
        }

        public String getDiffUrl()
        {
            return diffUrl;
        }

        public void setDiffUrl(String diffUrl)
        {
            this.diffUrl = diffUrl;
        }
    }
}
