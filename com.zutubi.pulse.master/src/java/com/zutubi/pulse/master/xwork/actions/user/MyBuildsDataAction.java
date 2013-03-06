package com.zutubi.pulse.master.xwork.actions.user;

import com.google.common.base.Function;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.project.BuildModel;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;

import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Action to return JSON data for the my builds page.
 */
public class MyBuildsDataAction extends ActionSupport
{
    private List<BuildModel> builds;

    private BuildManager buildManager;
    private ConfigurationManager configurationManager;
    
    public List<BuildModel> getBuilds()
    {
        return builds;
    }

    public String execute() throws Exception
    {
        String login = SecurityUtils.getLoggedInUsername();
        if (login == null)
        {
            return ERROR;
        }
        
        User user = userManager.getUser(login);
        if (user == null)
        {
            return ERROR;
        }

        builds = newArrayList(transform(buildManager.getPersonalBuilds(user), new Function<BuildResult, BuildModel>()
        {
            public BuildModel apply(BuildResult buildResult)
            {
                return new BuildModel(buildResult, new Urls(configurationManager.getSystemConfig().getContextPathNormalised()), false);
            }
        }));
        
        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
