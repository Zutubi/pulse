package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class ViewChangesAction extends BuildActionBase
{
    private static final int FILE_LIMIT = 5;

    private long sinceBuild = 0;
    private BuildResult previous;
    private BuildResult previousSuccessful;
    private BuildResult previousUnsuccessful;
    private BuildResult sinceResult;
    private List<ChangelistModel> changelists;

    public long getSinceBuild()
    {
        return sinceBuild;
    }

    public void setSinceBuild(long sinceBuild)
    {
        this.sinceBuild = sinceBuild;
    }

    public BuildResult getSinceResult()
    {
        return sinceResult;
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

    public List<ChangelistModel> getChangelists()
    {
        return changelists;
    }

    public String execute()
    {
        BuildResult result = getRequiredBuildResult();
        if(result.isPersonal())
        {
            return "personal";
        }

        previous = buildManager.getPreviousBuildResultWithRevision(result, ResultState.getCompletedStates());
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
            if(sinceBuild >= result.getNumber())
            {
                addActionError("Invalid build range");
                return ERROR;
            }

            sinceResult = buildManager.getByProjectAndNumber(result.getProject(), sinceBuild);
            if(sinceResult == null)
            {
                addActionError("No such build [" + sinceBuild + "]");
                return ERROR;
            }
        }

        List<PersistentChangelist> rawChangelists = new LinkedList<PersistentChangelist>();

        // Get changes for all results after since, up to and including to.
        if (sinceBuild != 0)
        {
            List<BuildResult> resultRange = buildManager.queryBuilds(result.getProject(), ResultState.getCompletedStates(), sinceBuild + 1, result.getNumber() - 1, 0, -1, true, false);
            for(BuildResult r: resultRange)
            {
                rawChangelists.addAll(buildManager.getChangesForBuild(r, true));
            }
        }
        rawChangelists.addAll(buildManager.getChangesForBuild(result, true));
        Collections.sort(rawChangelists, new ChangelistComparator());
        changelists = CollectionUtils.map(rawChangelists, new Mapping<PersistentChangelist, ChangelistModel>()
        {
            public ChangelistModel map(PersistentChangelist persistentChangelist)
            {
                return new ChangelistModel(persistentChangelist, buildManager.getChangelistSize(persistentChangelist), buildManager.getChangelistFiles(persistentChangelist, 0, FILE_LIMIT));
            }
        });
        
        previousSuccessful = buildManager.getPreviousBuildResultWithRevision(result, new ResultState[] { ResultState.SUCCESS });
        previousUnsuccessful = buildManager.getPreviousBuildResultWithRevision(result, ResultState.getBrokenStates());

        return SUCCESS;
    }
    
    public static class ChangelistModel
    {
        private PersistentChangelist changelist;
        private int changeCount;
        private List<PersistentFileChange> changes;

        public ChangelistModel(PersistentChangelist changelist, int changeCount, List<PersistentFileChange> changes)
        {
            this.changelist = changelist;
            this.changeCount = changeCount;
            this.changes = changes;
        }

        public PersistentChangelist getChangelist()
        {
            return changelist;
        }

        public int getChangeCount()
        {
            return changeCount;
        }

        public List<PersistentFileChange> getChanges()
        {
            return changes;
        }
    }
}
