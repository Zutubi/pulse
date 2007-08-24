package com.zutubi.pulse.webwork.mapping;

/**
 */
public class BrowseProjectActionResolver extends StaticMapActionResolver
{
    public BrowseProjectActionResolver(String project)
    {
        super("currentBuild");

        addMapping("home", new ProjectHomeActionResolver());
        addMapping("reports", new ProjectReportsActionResolver());
        addMapping("history", new ProjectHistoryActionResolver());
        addMapping("builds", new ProjectBuildsActionResolver());
        addMapping("changes", new ChangelistsActionResolver());

        addParameter("projectName", project);
    }
}
