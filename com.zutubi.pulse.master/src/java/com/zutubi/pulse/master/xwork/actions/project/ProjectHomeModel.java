package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.user.ChangelistModel;
import com.zutubi.util.TimeStamps;
import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * Model for JSON data used to render the project home page.
 */
public class ProjectHomeModel
{
    private ProjectResponsibilityModel responsibility;
    private StatusModel status;
    private List<BuildModel> activity = new LinkedList<BuildModel>();
    private BuildModel latest;
    private List<BuildModel> recent = new LinkedList<BuildModel>();
    private List<ChangelistModel> changes = new LinkedList<ChangelistModel>();
    private List<ActionLink> actions = new LinkedList<ActionLink>();
    private List<ActionLink> links = new LinkedList<ActionLink>();
    private String url;

    public ProjectHomeModel(StatusModel status)
    {
        this.status = status;
    }

    public ProjectResponsibilityModel getResponsibility()
    {
        return responsibility;
    }

    public void setResponsibility(ProjectResponsibilityModel responsibility)
    {
        this.responsibility = responsibility;
    }

    public StatusModel getStatus()
    {
        return status;
    }

    @JSON
    public List<BuildModel> getActivity()
    {
        return activity;
    }
    
    public BuildModel getLatest()
    {
        return latest;
    }

    public void setLatest(BuildModel latest)
    {
        this.latest = latest;
    }

    @JSON
    public List<BuildModel> getRecent()
    {
        return recent;
    }
    
    @JSON
    public List<ChangelistModel> getChanges()
    {
        return changes;
    }

    @JSON
    public List<ActionLink> getActions()
    {
        return actions;
    }
    
    public void addAction(ActionLink action)
    {
        actions.add(action);
    }
    
    @JSON
    public List<ActionLink> getLinks()
    {
        return links;
    }
    
    public void addLink(ActionLink link)
    {
        links.add(link);
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public static class StatusModel
    {
        private String name;
        private String health;
        private StateModel state;
        private int successRate;
        private StatisticsModel statistics;

        public StatusModel(String name, String health, StateModel state, StatisticsModel statistics)
        {
            this.name = name;
            this.health = health;
            this.state = state;
            this.successRate = (int) Math.round((statistics.getTotal() - statistics.getFailed()) * 100.0 / statistics.getTotal());
            this.statistics = statistics;
        }

        public String getName()
        {
            return name;
        }

        public String getHealth()
        {
            return health;
        }

        public StateModel getState()
        {
            return state;
        }

        public int getSuccessRate()
        {
            return successRate;
        }

        public StatisticsModel getStatistics()
        {
            return statistics;
        }
    }

    public static class StateModel
    {
        private String pretty;
        private boolean canPause;
        private boolean canResume;

        public StateModel(String pretty, boolean canPause, boolean canResume)
        {
            this.pretty = pretty;
            this.canPause = canPause;
            this.canResume = canResume;
        }

        public String getPretty()
        {
            return pretty;
        }

        public boolean isCanPause()
        {
            return canPause;
        }

        public boolean isCanResume()
        {
            return canResume;
        }
    }

    public static class StatisticsModel
    {
        private int total;
        private int ok;
        private int failed;

        public StatisticsModel(int total, int ok, int failed)
        {
            this.total = total;
            this.ok = ok;
            this.failed = failed;
        }

        public int getTotal()
        {
            return total;
        }

        public int getOk()
        {
            return ok;
        }

        public int getFailed()
        {
            return failed;
        }
    }
        
    public static class ProjectResponsibilityModel
    {
        private String owner; // foo is responsible for...
        private String comment; // optional
        private boolean canClear;

        public ProjectResponsibilityModel(String owner, String comment)
        {
            this.owner = owner;
            this.comment = comment;
        }

        public String getOwner()
        {
            return owner;
        }

        public String getComment()
        {
            return comment;
        }

        public boolean isCanClear()
        {
            return canClear;
        }

        public void setCanClear(boolean canClear)
        {
            this.canClear = canClear;
        }
    }
    
    public static class RevisionModel
    {
        private String revisionString;
        private String link;

        public RevisionModel(String revisionString)
        {
            this.revisionString = revisionString;
        }

        public String getRevisionString()
        {
            return revisionString;
        }

        public void setRevisionString(String revisionString)
        {
            this.revisionString = revisionString;
        }

        public String getLink()
        {
            return link;
        }
    }
    
    public static class DateModel
    {
        private String absolute;
        private String relative;

        public DateModel(TimeStamps stamps)
        {
            absolute = stamps.getPrettyStartDate();
            relative = stamps.getPrettyStartTime();
        }

        public String getAbsolute()
        {
            return absolute;
        }

        public String getRelative()
        {
            return relative;
        }
    }
    
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
    
    public static class BuildModel
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

        public BuildModel(BuildResult buildResult)
        {
            id = buildResult.getId();
            number = buildResult.getNumber();
            status = buildResult.getState().getPrettyString();
            reason = buildResult.getReason().getSummary();
            Revision buildRevision = buildResult.getRevision();
            revision = buildRevision == null ? null : new RevisionModel(buildRevision.getRevisionString());
            tests = buildResult.getTestSummary().toString();
            when = new DateModel(buildResult.getStamps());
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
    }
}
