package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.project.ProjectDependencyGraphRenderer;
import com.zutubi.util.WebUtils;

/**
 * The browse project dependencies page.
 */
public class ProjectDependenciesPage extends AbstractLogPage
{
    private static final String SECTION_UPSTREAM = "upstream";
    private static final String SECTION_DOWNSTREAM = "downstream";

    private static final String ID_TRANSITIVE_MODE_COMBO = "dependencies-transitive-mode";

    private String projectName;

    public ProjectDependenciesPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-dependencies-" + projectName);
        this.projectName = projectName;
    }

    public String getUrl()
    {
        return urls.projectDependencies(WebUtils.uriComponentEncode(projectName));
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("view.initialised");
    }

    public boolean isUpstreamPresent(String project, int x, int y)
    {
        return isProjectPresent(SECTION_UPSTREAM, project, x, y);
    }

    public String getUpstreamId(String project, int x, int y)
    {
        return getDependencyId(SECTION_UPSTREAM, project, x, y);
    }

    public boolean isDownstreamPresent(String project, int x, int y)
    {
        return isProjectPresent(SECTION_DOWNSTREAM, project, x, y);
    }

    public String getDownstreamId(String project, int x, int y)
    {
        return getDependencyId(SECTION_DOWNSTREAM, project, x, y);
    }

    /**
     * Gets the current transitive mode from the toolbar combo.
     * 
     * @return the current transitive mode
     */
    public ProjectDependencyGraphBuilder.TransitiveMode getTransitiveMode()
    {
        return ProjectDependencyGraphBuilder.TransitiveMode.valueOf(browser.getComboValue(ID_TRANSITIVE_MODE_COMBO));
    }

    /**
     * Selects the given transitive mode from the toolbar combo and waits for
     * the change to take effect.
     * 
     * @param mode the mode to set
     */
    public void setTransitiveModeAndWait(ProjectDependencyGraphBuilder.TransitiveMode mode)
    {
        browser.setComboByValue(ID_TRANSITIVE_MODE_COMBO, mode.name());
        browser.waitForVariable("panel.loading", true);
    }

    private boolean isProjectPresent(String section, String project, int x, int y)
    {
        return browser.isElementIdPresent(getDependencyId(section, project, x, y));
    }

    private String getDependencyId(String section, String project, int x, int y)
    {
        return "dependencies-" + section + "-" + (x * ProjectDependencyGraphRenderer.SCALE_FACTOR_X) + "-" + (y * ProjectDependencyGraphRenderer.SCALE_FACTOR_Y) + "-" + project;
    }
}
