package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.PersonalBuildRequestEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.project.BuildModel;
import com.zutubi.pulse.master.xwork.actions.project.RevisionModel;

import java.util.LinkedList;
import java.util.List;

/**
 * Models JSON data for the server activity tab.
 */
public class ServerActivityModel
{
    private boolean buildQueueTogglePermitted;
    private boolean buildQueueRunning;
    private boolean stageQueueTogglePermitted;
    private boolean stageQueueRunning;
    private List<QueuedBuildModel> queued = new LinkedList<QueuedBuildModel>();
    private List<ActiveBuildModel> active = new LinkedList<ActiveBuildModel>();

    public boolean isBuildQueueTogglePermitted()
    {
        return buildQueueTogglePermitted;
    }

    public void setBuildQueueTogglePermitted(boolean buildQueueTogglePermitted)
    {
        this.buildQueueTogglePermitted = buildQueueTogglePermitted;
    }

    public boolean isBuildQueueRunning()
    {
        return buildQueueRunning;
    }

    public void setBuildQueueRunning(boolean buildQueueRunning)
    {
        this.buildQueueRunning = buildQueueRunning;
    }

    public boolean isStageQueueTogglePermitted()
    {
        return stageQueueTogglePermitted;
    }

    public void setStageQueueTogglePermitted(boolean stageQueueTogglePermitted)
    {
        this.stageQueueTogglePermitted = stageQueueTogglePermitted;
    }

    public boolean isStageQueueRunning()
    {
        return stageQueueRunning;
    }

    public void setStageQueueRunning(boolean stageQueueRunning)
    {
        this.stageQueueRunning = stageQueueRunning;
    }

    public List<QueuedBuildModel> getQueued()
    {
        return queued;
    }

    public void addQueued(QueuedBuildModel model)
    {
        queued.add(model);
    }

    public List<ActiveBuildModel> getActive()
    {
        return active;
    }

    public void addActive(ActiveBuildModel model)
    {
        active.add(model);
    }

    public static class QueuedBuildModel
    {
        private long id;
        private String owner;
        private boolean personal;
        private long personalNumber;
        private RevisionModel revision;
        private String prettyQueueTime;
        private String reason;
        private boolean cancelPermitted;
        private boolean hidden;
        
        public QueuedBuildModel(BuildRequestEvent requestEvent, boolean cancelPermitted)
        {
            id = requestEvent.getId();
            owner = requestEvent.getOwner().getName();

            if (requestEvent instanceof PersonalBuildRequestEvent)
            {
                personal = true;
                personalNumber = ((PersonalBuildRequestEvent) requestEvent).getNumber();
                revision = new RevisionModel("[personal]");
            }
            else
            {
                personal = false;
                Revision revision = requestEvent.getRevision().getRevision();
                if (revision == null)
                {
                    this.revision = new RevisionModel("[floating]");
                }
                else
                {
                    this.revision = new RevisionModel(revision.getRevisionString());
                }
            }

            prettyQueueTime = requestEvent.getPrettyQueueTime();
            reason = requestEvent.getReason().getSummary();
            this.cancelPermitted = cancelPermitted;
            hidden = false;
        }

        public QueuedBuildModel(boolean personal)
        {
            this.personal = personal;
            hidden = true;
        }

        public long getId()
        {
            return id;
        }

        public String getOwner()
        {
            return owner;
        }

        public boolean isPersonal()
        {
            return personal;
        }

        public long getPersonalNumber()
        {
            return personalNumber;
        }

        public RevisionModel getRevision()
        {
            return revision;
        }

        public String getPrettyQueueTime()
        {
            return prettyQueueTime;
        }

        public String getReason()
        {
            return reason;
        }

        public boolean isCancelPermitted()
        {
            return cancelPermitted;
        }

        public boolean isHidden()
        {
            return hidden;
        }
    }

    public static class ActiveBuildModel extends BuildModel
    {
        private boolean cancelPermitted;
        private boolean hidden;

        public ActiveBuildModel(BuildResult buildResult, Urls urls, boolean cancelPermitted)
        {
            super(buildResult, urls, false);
            this.cancelPermitted = cancelPermitted;
            hidden = false;
        }

        public ActiveBuildModel(boolean personal)
        {
            super(0, 0, personal, null, null, null, null, null, null, null, null);
            hidden = true;
        }

        public boolean isCancelPermitted()
        {
            return cancelPermitted;
        }

        public boolean isHidden()
        {
            return hidden;
        }
    }
}
