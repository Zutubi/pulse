package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.BuildViewport;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import flexjson.JSON;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * An ajax action that provides build details to the build navigation
 * widget.
 *
 * @see BuildViewport
 */
public class GetBuildViewportAction extends ActionSupport
{
    /**
     * The id that identifies the build for which the
     * viewport will be retrieved.
     */
    private long buildId;

    /**
     * The data returned via JSON to the UI.
     */
    private Data data;

    private BuildManager buildManager;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public Data getData()
    {
        return data;
    }

    @Override
    public String execute() throws Exception
    {
        data = new Data();

        BuildViewport viewport = buildManager.getBuildViewport(buildId);

        List<BuildResult> builds = viewport.getVisibleBuilds();
        data.builds = CollectionUtils.map(builds, new Mapping<BuildResult, BuildData>()
        {
            public BuildData map(BuildResult result)
            {
                return new BuildData(result);
            }
        });

        BuildResult nextBrokenBuild = viewport.getNextBrokenBuild();
        if (nextBrokenBuild != null)
        {
            data.nextBroken = new BuildData(nextBrokenBuild);
        }
        BuildResult nextSuccessfulBuild = viewport.getNextSuccessfulBuild();
        if (nextSuccessfulBuild != null)
        {
            data.nextSuccessful = new BuildData(nextSuccessfulBuild);
        }
        BuildResult previousBrokenBuild = viewport.getPreviousBrokenBuild();
        if (previousBrokenBuild != null)
        {
            data.previousBroken = new BuildData(previousBrokenBuild);
        }
        BuildResult previousSuccessfulBuild = viewport.getPreviousSuccessfulBuild();
        if (previousSuccessfulBuild != null)
        {
            data.previousSuccessful = new BuildData(previousSuccessfulBuild);
        }

        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    /**
     * The data object that defines the format of the returned result.
     */
    public static class Data implements Serializable
    {
        private List<BuildData> builds = new LinkedList<BuildData>();

        private BuildData nextSuccessful;
        private BuildData nextBroken;
        private BuildData previousSuccessful;
        private BuildData previousBroken;

        @JSON
        public List<BuildData> getBuilds()
        {
            return builds;
        }

        public BuildData getNextSuccessful()
        {
            return nextSuccessful;
        }

        public BuildData getNextBroken()
        {
            return nextBroken;
        }

        public BuildData getPreviousSuccessful()
        {
            return previousSuccessful;
        }

        public BuildData getPreviousBroken()
        {
            return previousBroken;
        }
    }

    public static class BuildData implements Serializable
    {
        private String status;
        private String id;
        private String number;
        private String name;

        public BuildData(BuildResult r)
        {
            status = r.getStateName();
            id = String.valueOf(r.getId());
            number = String.valueOf(r.getNumber());
            name = r.getProject().getName();
        }

        public String getNumber()
        {
            return number;
        }

        public String getStatus()
        {
            return status;
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }
    }
}
