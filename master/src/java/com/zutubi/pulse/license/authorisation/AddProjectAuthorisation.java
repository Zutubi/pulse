package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.license.License;

/**
 * <class-comment/>
 */
public class AddProjectAuthorisation implements Authorisation
{
    private ProjectManager projectManager;

    public static final String[] AUTH = {"canAddProject"};

    public String[] getAuthorisation(License license)
    {
        if (license == null)
        {
            return new String[0];
        }

        if (license.getSupportedProjects() == License.UNRESTRICTED)
        {
            return AUTH;
        }

        if (projectManager.getProjectCount() < license.getSupportedProjects())
        {
            return AUTH;
        }
        return new String[0];
    }

    /**
     * Required resource.
     *
     * @param projectManager
     */
    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
