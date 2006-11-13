package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.MavenPulseFileDetails;
import com.zutubi.pulse.model.Maven2PulseFileDetails;
import com.opensymphony.util.TextUtils;

/**
 *
 *
 */
public class EditMaven2DetailsAction extends AbstractEditDetailsAction
{
    private Maven2PulseFileDetails details = new Maven2PulseFileDetails();

    public void prepare()
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), Maven2PulseFileDetails.class);
    }

    public PulseFileDetails getDetails()
    {
        if (!TextUtils.stringSet(details.getWorkingDir()))
        {
            details.setWorkingDir(null);
        }

        if (!TextUtils.stringSet(details.getGoals()))
        {
            details.setGoals(null);
        }

        if (!TextUtils.stringSet(details.getArguments()))
        {
            details.setArguments(null);
        }

        return details;
    }
}
