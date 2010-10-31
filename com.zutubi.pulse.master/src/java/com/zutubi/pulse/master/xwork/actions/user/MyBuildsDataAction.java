package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.project.BuildModel;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.List;

/**
 * Action to return JSON data for the my builds page.
 */
public class MyBuildsDataAction extends ActionSupport
{
    private List<BuildModel> builds;

    private BuildManager buildManager;
    
    public List<BuildModel> getBuilds()
    {
        return builds;
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUsername();
        if (login == null)
        {
            return ERROR;
        }
        
        User user = userManager.getUser(login);
        if (user == null)
        {
            return ERROR;
        }

        builds = CollectionUtils.map(buildManager.getPersonalBuilds(user), new Mapping<BuildResult, BuildModel>()
        {
            public BuildModel map(BuildResult buildResult)
            {
                return new BuildModel(buildResult);
            }
        });
        
        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
