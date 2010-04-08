package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.util.StringUtils;

/**
 * Action to save tail view settings to a user's preferences.  Invalid settings
 * are ignored.
 */
public class SaveArtifactsFilterAction extends ActionSupport
{
    private String filter;
    private SimpleResult result;

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    @Override
    public String execute() throws Exception
    {
        Object principle = getPrinciple();
        if (principle != null)
        {
            User user = userManager.getUser((String) principle);
            if (user != null && !StringUtils.equals(user.getArtifactsFilter(), filter))
            {
                user.setArtifactsFilter(filter);
                userManager.save(user);
            }
        }

        result = new SimpleResult(true, "filter saved");
        return SUCCESS;
    }
}