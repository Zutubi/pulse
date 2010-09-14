package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.BuildViewport;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.util.StringUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.List;

/**
 * Helper base class for actions under:
 *   browse/projects/&lt;project name&gt;/builds/&lt;build id&gt;/ <b>or</b>
 *   dashboard/my/&lt;build id &gt;/
 */
public class BuildActionBase extends ProjectActionBase
{
    private boolean personal;
    private String buildVID;
    private BuildResult buildResult;
    private Viewport viewport;

    public boolean isPersonal()
    {
        return personal;
    }

    public void setPersonal(boolean personal)
    {
        this.personal = personal;
    }

    public String getBuildVID()
    {
        return buildVID;
    }

    public void setBuildVID(String buildVID)
    {
        this.buildVID = buildVID;
    }

    public long getBuildId()
    {
        BuildResult result = getBuildResult();
        if (result == null)
        {
            return 0;
        }
        else
        {
            return result.getId();
        }
    }
    
    public BuildResult getBuildResult()
    {
        if (buildResult == null)
        {
            if (isPersonal())
            {
                User user = getLoggedInUser();
                if(user == null)
                {
                    throw new LookupErrorException("Cannot look up personal builds when not logged in");
                }

                if(StringUtils.stringSet(buildVID))
                {
                    buildResult = buildManager.getByUserAndVirtualId(user, buildVID);
                    if(buildResult == null)
                    {
                        throw new LookupErrorException("Unknown personal build [" + buildVID + "]");
                    }

                    setProjectName(buildResult.getProject().getName());
                }
            }
            else
            {
                Project project = getProject();
                if (project != null)
                {
                    if (StringUtils.stringSet(buildVID))
                    {
                        buildResult = buildManager.getByProjectAndVirtualId(project, buildVID);
                        if(buildResult == null)
                        {
                            throw new LookupErrorException("Unknown build [" + buildVID + "] for project [" + project.getName() + "]");
                        }
                    }
                }
            }
        }

        return buildResult;
    }

    public BuildResult getRequiredBuildResult()
    {
        BuildResult result = getBuildResult();
        if(result == null)
        {
            throw new LookupErrorException("Build number required");
        }

        return buildResult;
    }

    public Viewport getViewport()
    {
        if (viewport == null)
        {
            loadBuildNavViewport();
        }
        return viewport;
    }

    private void loadBuildNavViewport()
    {
        viewport = new Viewport();

        BuildViewport buildViewport = objectFactory.buildBean(BuildViewport.class,
                new Class[]{Long.TYPE}, new Object[]{getBuildId()}
        );

        List<BuildResult> builds = buildViewport.getVisibleBuilds();
        viewport.addAll(CollectionUtils.map(builds, new Mapping<BuildResult, Viewport.Data>()
        {
            public Viewport.Data map(BuildResult result)
            {
                return new Viewport.Data(result);
            }
        }));

        // provide the details for the links.
        BuildResult nextBrokenBuild = buildViewport.getNextBrokenBuild();
        if (nextBrokenBuild != null)
        {
            viewport.setNextBroken(new Viewport.Data(nextBrokenBuild));
        }
        BuildResult nextSuccessfulBuild = buildViewport.getNextSuccessfulBuild();
        if (nextSuccessfulBuild != null)
        {
            viewport.setNextSuccessful(new Viewport.Data(nextSuccessfulBuild));
        }
        BuildResult previousBrokenBuild = buildViewport.getPreviousBrokenBuild();
        if (previousBrokenBuild != null)
        {
            viewport.setPreviousBroken(new Viewport.Data(previousBrokenBuild));
        }
        BuildResult previousSuccessfulBuild = buildViewport.getPreviousSuccessfulBuild();
        if (previousSuccessfulBuild != null)
        {
            viewport.setPreviousSuccessful(new Viewport.Data(previousSuccessfulBuild));
        }
        BuildResult latestBuild = buildViewport.getLatestBuild();
        if (latestBuild != null && latestBuild.getId() != getBuildId())
        {
            viewport.setLatest(new Viewport.Data(latestBuild));
        }
    }

}
