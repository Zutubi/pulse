package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.license.License;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class PreferencesAction extends UserActionSupport
{
    private ProjectManager projectManager;

    public int getProjectCount()
    {
        return projectManager.getProjectCount();
    }
    
    public String getRefreshInterval()
    {
        long refreshInterval = getUser().getRefreshInterval();
        if (refreshInterval == User.REFRESH_DISABLED)
        {
            return getText("user.refresh.never", "never");
        }
        else
        {
            return getText("user.refresh.every", Arrays.asList(new Object [] { getUser().getRefreshInterval() } ));
        }
    }

    public String getTailRefreshInterval()
    {
        return getText("user.refresh.every", Arrays.asList(new Object [] { getUser().getTailRefreshInterval() } ));
    }

    public String doInput() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return "guest";
        }

        setUserLogin(login);

        // load the user from the db.
        User user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return ERROR;
        }
        return super.doInput();
    }

    /**
     * This needs to be moved into the licensing
     */
    public boolean canAddContact()
    {
        int supportedContactPoints = LicenseHolder.getLicense().getSupportedContactPoints();
        if (supportedContactPoints != License.UNRESTRICTED)
        {
            return getUser().getContactPoints().size() < supportedContactPoints;
        }
        return true;
    }

    public String execute() throws Exception
    {
        String result = doInput();
        if (result.equals(INPUT))
        {
            return SUCCESS;
        }
        return result;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
