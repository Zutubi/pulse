package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.links.ConfigurationLink;
import com.zutubi.util.WebUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Links from the project configuration page to related project pages.
 */
public class ProjectConfigurationLinks
{
    public List<ConfigurationLink> getLinks(ProjectConfiguration projectConfiguration)
    {
        if (projectConfiguration.isConcrete())
        {
            Urls urls = Urls.getBaselessInstance();
            String name = WebUtils.uriComponentEncode(projectConfiguration.getName());
            return Arrays.asList(
                    new ConfigurationLink("home", urls.projectHome(name)),
                    new ConfigurationLink("reports", urls.projectReports(name)),
                    new ConfigurationLink("history", urls.projectHistory(name)),
                    new ConfigurationLink("dependencies", urls.projectDependencies(name)),
                    new ConfigurationLink("log", urls.projectLog(name))
            );
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
