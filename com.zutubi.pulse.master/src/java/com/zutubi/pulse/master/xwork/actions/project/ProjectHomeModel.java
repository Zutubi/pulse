package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.committransformers.CommitMessageSupport;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.model.ActionLink;
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
    private String description;
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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
            // errors / terminated states are excluded from the success rate statistic
            this.successRate = (int) Math.round((statistics.getOk()) * 100.0 / (statistics.getFailed() + statistics.getOk()));
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
        private String keyTransition;

        public StateModel(String pretty, String keyTransition)
        {
            this.pretty = pretty;
            this.keyTransition = keyTransition;
        }

        public String getPretty()
        {
            return pretty;
        }

        public String getKeyTransition()
        {
            return keyTransition;
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

    public static class ChangelistCommentModel
    {
        private static final int COMMENT_LINE_LENGTH = 80;
        private static final int COMMENT_TRIM_LIMIT = 60;

        private String abbreviated;
        private String comment;

        public ChangelistCommentModel(CommitMessageSupport commitMessageSupport)
        {
            if (commitMessageSupport.getLength() > COMMENT_TRIM_LIMIT)
            {
                abbreviated = commitMessageSupport.trim(COMMENT_TRIM_LIMIT);
            }

            this.comment = commitMessageSupport.wrap(COMMENT_LINE_LENGTH);
        }

        public String getAbbreviated()
        {
            return abbreviated;
        }

        public String getComment()
        {
            return comment;
        }
    }

    public static class ChangelistModel
    {
        private long id;
        private RevisionModel revision;
        private String who;
        private DateModel when;
        private ChangelistCommentModel comment;

        public ChangelistModel(PersistentChangelist changelist, ProjectConfiguration projectConfig)
        {
            id = changelist.getId();
            revision = new RevisionModel(changelist.getRevision(), projectConfig.getChangeViewer());
            who = changelist.getAuthor();
            when = new DateModel(changelist.getTime());
            comment = new ChangelistCommentModel(new CommitMessageSupport(changelist.getComment(), projectConfig.getCommitMessageTransformers().values()));
        }

        public long getId()
        {
            return id;
        }

        public RevisionModel getRevision()
        {
            return revision;
        }

        public String getWho()
        {
            return who;
        }

        public DateModel getWhen()
        {
            return when;
        }

        public ChangelistCommentModel getComment()
        {
            return comment;
        }
    }
}
