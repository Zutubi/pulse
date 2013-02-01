package com.zutubi.pulse.master.xwork.actions.user;

import com.google.common.base.Function;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.committransformers.CommitMessageSupport;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.util.CollectionUtils;
import flexjson.JSON;

import java.util.List;

/**
 * JSON-encodable object representing a changelist.
 */
public class ChangelistModel
{
    private static final int COMMENT_LINE_LENGTH = 80;
    private static final int COMMENT_TRIM_LIMIT = 60;

    private PersistentChangelist changelist;
    private String url;
    private String aggregateStatusClass;
    private String aggregateStatus;
    private String aggregateStatusIcon;
    private List<ChangelistBuildModel> builds;
    private CommitMessageSupport commitMessageSupport;

    /**
     * The build result aggregation order
     */
    private static final ResultState[] AGGREGATION_ORDER = new ResultState[]{ResultState.SUCCESS, ResultState.WARNINGS, ResultState.IN_PROGRESS, ResultState.FAILURE, ResultState.ERROR, ResultState.TERMINATED};

    public ChangelistModel(PersistentChangelist changelist, String url, List<BuildResult> buildResults, CommitMessageSupport commitMessageSupport)
    {
        this.changelist = changelist;
        this.url = url;
        this.builds = CollectionUtils.map(buildResults, new Function<BuildResult, ChangelistBuildModel>()
        {
            public ChangelistBuildModel apply(BuildResult buildResult)
            {
                return new ChangelistBuildModel(buildResult);
            }
        });

        this.commitMessageSupport = commitMessageSupport;

        // Failed states trump everything, in progress trumps success
        ResultState aggregrateState = ResultState.SUCCESS;
        for (BuildResult build: buildResults)
        {
            aggregrateState = ResultState.getAggregate(aggregrateState, build.getState(), AGGREGATION_ORDER);
        }

        aggregateStatus = aggregrateState.getPrettyString();
        aggregateStatusClass= ToveUtils.getStatusClass(aggregrateState);
        aggregateStatusIcon = ToveUtils.getStatusIcon(aggregrateState);
    }

    public long getId()
    {
        return changelist.getId();
    }

    public String getUrl()
    {
        return url;
    }

    public String getRevision()
    {
        return changelist.getRevision().getRevisionString();
    }

    public String getShortRevision()
    {
        Revision revision = changelist.getRevision();
        if (revision.isAbbreviated())
        {
            return revision.getAbbreviatedRevisionString();
        }
        else
        {
            return null;
        }
    }

    public String getWho()
    {
        return changelist.getAuthor();
    }

    public String getTime()
    {
        return changelist.getPrettyTime();
    }

    public String getDate()
    {
        return changelist.getPrettyDate(ActionContext.getContext().getLocale());
    }

    public String getComment()
    {
        return commitMessageSupport.wrap(COMMENT_LINE_LENGTH);
    }

    public String getShortComment()
    {
        if (commitMessageSupport.getLength() > COMMENT_TRIM_LIMIT)
        {
            return commitMessageSupport.trim(COMMENT_TRIM_LIMIT);
        }
        else
        {
            return null;
        }
    }

    public String getAggregateStatusClass()
    {
        return aggregateStatusClass;
    }

    public String getAggregateStatus()
    {
        return aggregateStatus;
    }

    public String getAggregateStatusIcon()
    {
        return aggregateStatusIcon;
    }
    
    public int getBuildCount()
    {
        return builds.size();
    }

    @JSON
    public List<ChangelistBuildModel> getBuilds()
    {
        return builds;
    }
}
