package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseHolder;

/**
 * <class-comment/>
 */
public class AddProjectAuthorisation implements Authorisation
{
    private ProjectManager projectManager;

    public static final String[] AUTH = {LicenseHolder.AUTH_ADD_PROJECT};

    public String[] getAuthorisation(License license)
    {
        if (license == null)
        {
            return NO_AUTH;
        }

        if (license.getSupportedProjects() == License.UNRESTRICTED)
        {
            return AUTH;
        }

        if (projectManager.getProjectCount() < license.getSupportedProjects())
        {
            return AUTH;
        }
        return NO_AUTH;
    }

    /**
     * Required resource.
     *
     * @param projectManager instance
     */
    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
