package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.links.ConfigurationLink;

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
            Urls urls = new Urls("");
            String name = projectConfiguration.getName();
            return Arrays.asList(
                    new ConfigurationLink("home", urls.projectHome(name), "house.gif"),
                    new ConfigurationLink("reports", urls.projectReports(name), "chart_bar.gif"),
                    new ConfigurationLink("history", urls.projectHistory(name), "time.gif"),
                    new ConfigurationLink("log", urls.projectLog(name), "script.gif")
            );
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
