package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * JSON data about a build, sufficient to summarise it.
 */
public class BuildModel extends ResultModel
{
    private long number;
    private boolean personal;
    private String project;
    private String owner;
    private String prettyQueueTime;
    private String reason;
    private RevisionModel revision;
    private String link;
    private String tests;
    private String maturity;
    private boolean pinned;
    private List<BuildStageModel> stages = new LinkedList<BuildStageModel>();

    public BuildModel(long id, long number, boolean personal, String project, String owner, String status, String prettyQueueTime, String reason, RevisionModel revision, String maturity)
    {
        super(id, status);
        this.number = number;
        this.personal = personal;
        this.project = project;
        this.owner = owner;
        this.prettyQueueTime = prettyQueueTime;
        this.reason = reason;
        this.maturity = maturity;
    }

    public BuildModel(BuildResult buildResult, Urls urls, boolean collectArtifacts)
    {
        this(buildResult, urls, collectArtifacts, buildResult.getProject().getConfig().getChangeViewer());
    }
    
    public BuildModel(BuildResult buildResult, Urls urls, boolean collectArtifacts, ChangeViewerConfiguration changeViewerConfig)
    {
        super(buildResult);
        number = buildResult.getNumber();
        personal = buildResult.isPersonal();
        project = buildResult.getProject().getName();
        owner = buildResult.getOwner().getName();
        reason = buildResult.getReason().getSummary();
        Revision buildRevision = buildResult.getRevision();
        if (buildRevision != null)
        {
            revision = new RevisionModel(buildRevision, changeViewerConfig);
        }
        tests = buildResult.getTestSummary().toString();
        
        for (RecipeResultNode node: buildResult.getRoot().getChildren())
        {
            stages.add(new BuildStageModel(buildResult, node, urls, collectArtifacts));
        }
        
        if (!personal || ((User) buildResult.getOwner()).getLogin().equals(SecurityUtils.getLoggedInUsername()))
        {
            link = Urls.getRelativeInstance().build(buildResult);
        }

        maturity = buildResult.getStatus();
        pinned = buildResult.isPinned();
    }

    public long getNumber()
    {
        return number;
    }

    public boolean isPersonal()
    {
        return personal;
    }

    public String getProject()
    {
        return project;
    }

    public String getOwner()
    {
        return owner;
    }

    public String getPrettyQueueTime()
    {
        return prettyQueueTime;
    }

    public String getReason()
    {
        return reason;
    }

    public RevisionModel getRevision()
    {
        return revision;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getTests()
    {
        return tests;
    }

    public String getMaturity()
    {
        return maturity;
    }

    public boolean isPinned()
    {
        return pinned;
    }

    public void setPinned(boolean pinned)
    {
        this.pinned = pinned;
    }

    @JSON
    public List<BuildStageModel> getStages()
    {
        return stages;
    }

    public BuildStageModel getStage(final String stageName)
    {
        return CollectionUtils.find(stages, new Predicate<BuildStageModel>()
        {
            public boolean satisfied(BuildStageModel buildStageModel)
            {
                return buildStageModel.getName().equals(stageName);
            }
        });
    }
}
