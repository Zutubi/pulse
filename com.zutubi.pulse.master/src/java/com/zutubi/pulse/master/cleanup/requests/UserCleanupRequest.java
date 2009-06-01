package com.zutubi.pulse.master.cleanup.requests;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.BuildCleanupOptions;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;

import java.util.List;

/**
 * A request to clean up the personal builds for a specific user.  The
 * number of builds retained is defined by the users preferences.
 */
public class UserCleanupRequest extends EntityCleanupRequest
{
    private BuildResultDao buildResultDao;
    private BuildManager buildManager;

    private User user;

    public UserCleanupRequest(User user)
    {
        super(user);
        this.user = user;
    }

    public void run()
    {
        List<BuildResult> builds = buildResultDao.getOldestCompletedBuilds(user, user.getPreferences().getMyBuildsCount());
        for(BuildResult build: builds)
        {
            BuildCleanupOptions options = new BuildCleanupOptions();
            options.setCleanBuildArtifacts(true);
            options.setCleanRepositoryArtifacts(true);
            options.setCleanDatabase(true);
            options.setCleanWorkDir(true);
            
            buildManager.cleanup(build, options);
        }
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
