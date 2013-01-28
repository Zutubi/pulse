package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildGraph;
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
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.DAGraph;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

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
        PersistentChangelist changelist = changelistManager.getChangelist(id);
        if (changelist == null)
        {
            throw new LookupErrorException("Unknown changelist [" + id + "]");
        }

        int fileCount = changelistManager.getChangelistSize(changelist);
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

        List<BuildGraph> buildGraphs = changelistManager.getAffectedBuilds(changelist);
        if (contextProject == null)
        {
            projectWithChangeViewer = findProjectWithChangeViewer(buildGraphs);
        }

        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        List<TreeBuildModel> treeBuilds = new LinkedList<TreeBuildModel>();
        List<DAGraph.Node<BuildResult>> roots = CollectionUtils.map(buildGraphs, new DAGraph.ToRootMapping<BuildResult>());
        Comparator<DAGraph.Node<BuildResult>> nodeComparator = new DAGraph.Node.CompareByData<BuildResult>(new BuildResult.CompareByOwnerThenNumber());
        Collections.sort(roots, nodeComparator);
        for (DAGraph.Node<BuildResult> root: roots)
        {
            addBuilds(root, nodeComparator, urls, treeBuilds, 0);
        }

        ChangeViewerConfiguration changeViewer = projectWithChangeViewer == null ? null : projectWithChangeViewer.getConfig().getChangeViewer();
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
        model = new ChangelistViewModel(changelistModel, treeBuilds, new PagerModel(fileCount, FILES_PER_PAGE, startPage));

        List<PersistentFileChange> files = changelistManager.getChangelistFiles(changelist, startPage * FILES_PER_PAGE, FILES_PER_PAGE);
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

    private void addBuilds(DAGraph.Node<BuildResult> node, Comparator<DAGraph.Node<BuildResult>> nodeComparator, Urls urls, List<TreeBuildModel> treeBuilds, int depth)
    {
        treeBuilds.add(new TreeBuildModel(node.getData(), urls, depth));
        List<DAGraph.Node<BuildResult>> connected = new LinkedList<DAGraph.Node<BuildResult>>(node.getConnected());
        Collections.sort(connected, nodeComparator);
        for (DAGraph.Node<BuildResult> child: connected)
        {
            addBuilds(child, nodeComparator, urls, treeBuilds, depth + 1);
        }
    }

    private Project findProjectWithChangeViewer(List<BuildGraph> buildGraphs)
    {
        for (BuildGraph buildGraph: buildGraphs)
        {
            DAGraph.Node<BuildResult> nodeWithChangeViewer = buildGraph.findNodeByPredicate(new Predicate<DAGraph.Node<BuildResult>>()
            {
                public boolean apply(DAGraph.Node<BuildResult> buildResultNode)
                {
                    return buildResultNode.getData().getProject().getConfig().getChangeViewer() != null;
                }
            });
            
            if (nodeWithChangeViewer != null)
            {
                return nodeWithChangeViewer.getData().getProject();
            }
        }
        
        return null;
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

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public static class TreeBuildModel extends BuildModel
    {
        private int depth;

        public TreeBuildModel(BuildResult buildResult, Urls urls, int depth)
        {
            super(buildResult, urls, false);
            this.depth = depth;
        }

        public int getDepth()
        {
            return depth;
        }
    }
    
    public static class ChangelistViewModel
    {
        private ChangelistModel changelist;
        private List<TreeBuildModel> builds;
        private List<ChangelistFileModel> files = new LinkedList<ChangelistFileModel>();
        private PagerModel pager;

        public ChangelistViewModel(ChangelistModel changelist, List<TreeBuildModel> builds, PagerModel pager)
        {
            this.changelist = changelist;
            this.builds = builds;
            this.pager = pager;
        }

        public ChangelistModel getChangelist()
        {
            return changelist;
        }

        public List<TreeBuildModel> getBuilds()
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
