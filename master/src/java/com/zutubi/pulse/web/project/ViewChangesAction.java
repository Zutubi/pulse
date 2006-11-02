package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class ViewChangesAction extends ProjectActionSupport
{
    private long id = 0;
    private long sinceBuild = 0;
    private long toBuild;
    private Project project;
    private BuildResult previous;
    private BuildResult previousSuccessful;
    private BuildResult previousUnsuccessful;
    private BuildResult sinceResult;
    private BuildResult result;
    private List<Changelist> changelists;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getSinceBuild()
    {
        return sinceBuild;
    }

    public void setSinceBuild(long sinceBuild)
    {
        this.sinceBuild = sinceBuild;
    }

    public long getToBuild()
    {
        return toBuild;
    }

    public void setToBuild(long toBuild)
    {
        this.toBuild = toBuild;
    }

    public BuildResult getSinceResult()
    {
        return sinceResult;
    }

    public BuildResult getResult()
    {
        return result;
    }

    public BuildResult getPrevious()
    {
        return previous;
    }

    public BuildResult getPreviousSuccessful()
    {
        return previousSuccessful;
    }

    public BuildResult getPreviousUnsuccessful()
    {
        return previousUnsuccessful;
    }

    public List<Changelist> getChangelists()
    {
        return changelists;
    }

    public void validate()
    {
        if(id == 0)
        {
            project = getProject();
            if(project == null)
            {
                addActionError("Unknown project [" + projectId + "]");
                return;
            }

            result = getBuildManager().getByProjectAndNumber(project, toBuild);
            if(result == null)
            {
                addActionError("No such build [" + toBuild + "]");
                return;
            }
        }
        else
        {
            result = getBuildManager().getBuildResult(id);
            if(result == null)
            {
                addActionError("Unknown build [" + id + "]");
                return;
            }

            toBuild = result.getNumber();
            project = result.getProject();
            projectId = project.getId();
        }

        previous = getPrevious(ResultState.getCompletedStates());
        if(sinceBuild == 0)
        {
            sinceResult = previous;
            if(previous != null)
            {
                sinceBuild = sinceResult.getNumber();
            }
        }
        else
        {
            sinceResult = getBuildManager().getByProjectAndNumber(project, sinceBuild);
            if(sinceResult == null)
            {
                addActionError("No such build [" + sinceBuild + "]");
            }

            if(sinceBuild >= toBuild)
            {
                addActionError("Invalid build range");
            }

            if(sinceResult != null && result != null && !sinceResult.getBuildSpecification().equals(result.getBuildSpecification()))
            {
                addActionError("Builds must be of the same specification");
            }
        }
    }

    private BuildResult getPrevious(ResultState[] states)
    {
        if(toBuild == 1)
        {
            return null;
        }

        List<BuildResult> previousResults = getBuildManager().querySpecificationBuilds(project, result.getBuildSpecification(), states, -1, toBuild - 1, 0, 1, true, false);
        if(previousResults.size() > 0)
        {
            return previousResults.get(0);
        }
        else
        {
            return null;
        }
    }

    public String execute()
    {
        changelists = new LinkedList<Changelist>();

        // Get changes for all results after since, up to and including to.
        if (sinceBuild != 0)
        {
            List<BuildResult> resultRange = getBuildManager().querySpecificationBuilds(project, result.getBuildSpecification(), ResultState.getCompletedStates(), sinceBuild + 1, toBuild - 1, 0, -1, true, false);
            for(BuildResult r: resultRange)
            {
                changelists.addAll(getBuildManager().getChangesForBuild(r));
            }
        }
        changelists.addAll(getBuildManager().getChangesForBuild(result));
        Collections.sort(changelists, new ChangelistComparator());
        
        previousSuccessful = getPrevious(new ResultState[] { ResultState.SUCCESS });
        previousUnsuccessful = getPrevious(new ResultState[] { ResultState.ERROR, ResultState.FAILURE });

        return SUCCESS;
    }
}
