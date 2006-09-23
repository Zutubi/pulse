package com.zutubi.pulse.web.project;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;

import java.util.List;

/**
 * 
 *
 */
public class ViewBuildAction extends ProjectActionSupport
{
    private long id;
    private BuildResult result;
    private List<Changelist> changelists;
    private long selectedNode;
    private CommitMessageHelper commitMessageHelper;
    private MasterConfigurationManager configurationManager;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getSelectedNode()
    {
        return selectedNode;
    }

    public void setSelectedNode(long selectedNode)
    {
        this.selectedNode = selectedNode;
    }

    public boolean haveSelectedNode()
    {
        return selectedNode != 0L && selectedNode != result.getId();
    }

    public Project getProject()
    {
        return result.getProject();
    }

    public BuildResult getResult()
    {
        return result;
    }

    public void validate()
    {

    }

    public String execute()
    {
        result = getBuildManager().getBuildResult(id);
        if (result == null)
        {
            addActionError("Unknown build [" + id + "]");
            return ERROR;
        }

        result.loadFailedTestResults(configurationManager.getDataDirectory(), 50);

        return SUCCESS;
    }

    public List<Changelist> getChangelists()
    {
        if(changelists == null)
        {
            changelists = getBuildManager().getChangesForBuild(getResult());
        }
        return changelists;
    }

    public String transformComment(Changelist changelist)
    {
        if(commitMessageHelper == null)
        {
            commitMessageHelper = new CommitMessageHelper(getProjectManager().getCommitMessageTransformers());
        }

        return commitMessageHelper.applyTransforms(changelist, 60);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
