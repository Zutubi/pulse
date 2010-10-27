package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.TimeStamps;
import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * JSON data about a build, sufficient to summarise it.
 */
public class BuildModel
{
    private long id;
    private long number;
    private String status;
    private String prettyQueueTime;
    private String reason;
    private RevisionModel revision;
    private String link;
    private String tests;
    private DateModel when;
    private ElapsedModel elapsed;
    private int errors = -1;
    private int warnings = -1;
    private List<StageModel> stages = new LinkedList<StageModel>();

    public BuildModel(long id, long number, String status, String prettyQueueTime, String reason, RevisionModel revision)
    {
        this.id = id;
        this.number = number;
        this.status = status;
        this.prettyQueueTime = prettyQueueTime;
        this.reason = reason;
        this.revision = revision;
    }

    public BuildModel(BuildResult buildResult, ChangeViewerConfiguration changeViewerConfig)
    {
        id = buildResult.getId();
        number = buildResult.getNumber();
        status = buildResult.getState().getPrettyString();
        reason = buildResult.getReason().getSummary();
        Revision buildRevision = buildResult.getRevision();
        if (buildRevision != null)
        {
            revision = new RevisionModel(buildRevision, changeViewerConfig);
        }
        tests = buildResult.getTestSummary().toString();
        when = new DateModel(buildResult.getStamps().getStartTime());
        elapsed = new ElapsedModel(buildResult.getStamps());
        errors = buildResult.getErrorFeatureCount();
        warnings = buildResult.getWarningFeatureCount();
        
        for (RecipeResultNode node: buildResult.getRoot().getChildren())
        {
            stages.add(new StageModel(node));
        }
        
        link = Urls.getBaselessInstance().build(buildResult).substring(1);
    }

    public long getId()
    {
        return id;
    }

    public long getNumber()
    {
        return number;
    }

    public String getStatus()
    {
        return status;
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

    public DateModel getWhen()
    {
        return when;
    }

    public ElapsedModel getElapsed()
    {
        return elapsed;
    }

    public int getErrors()
    {
        return errors;
    }

    public int getWarnings()
    {
        return warnings;
    }

    @JSON
    public List<StageModel> getStages()
    {
        return stages;
    }

    /**
     * Defines JSON data for a build stage.
     */
    public static class StageModel
    {
        private String name;
        private String status;

        public StageModel(RecipeResultNode node)
        {
            name = node.getStageName();
            status = node.getResult().getState().getPrettyString();
        }

        public String getName()
        {
            return name;
        }

        public String getStatus()
        {
            return status;
        }
    }

    /**
     * Defines JSON data for a build elapsed time.
     */
    public static class ElapsedModel
    {
        private String prettyElapsed;
        private String prettyEstimatedTimeRemaining;
        private int estimatedPercentComplete;
    
        public ElapsedModel(TimeStamps stamps)
        {
            prettyElapsed = TimeStamps.getPrettyElapsed(stamps.getElapsed(), 2);
            if (stamps.hasEstimatedTimeRemaining())
            {
                prettyEstimatedTimeRemaining = stamps.getPrettyEstimatedTimeRemaining();
                estimatedPercentComplete = stamps.getEstimatedPercentComplete();
            }
        }
    
        public String getPrettyElapsed()
        {
            return prettyElapsed;
        }
    
        public String getPrettyEstimatedTimeRemaining()
        {
            return prettyEstimatedTimeRemaining;
        }
    
        public int getEstimatedPercentComplete()
        {
            return estimatedPercentComplete;
        }
    }
}
