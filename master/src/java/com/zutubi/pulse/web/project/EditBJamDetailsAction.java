package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.model.BJamPulseFileDetails;
import com.zutubi.pulse.model.PulseFileDetails;

/**
 * Action for editing properties of a boost jam project.
 */
public class EditBJamDetailsAction extends AbstractEditDetailsAction
{
    private BJamPulseFileDetails details = new BJamPulseFileDetails();

    public void prepare()
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), BJamPulseFileDetails.class);
    }

    public PulseFileDetails getDetails()
    {
        if (!TextUtils.stringSet(details.getJamfile()))
        {
            details.setJamfile(null);
        }

        if (!TextUtils.stringSet(details.getTargets()))
        {
            details.setTargets(null);
        }

        if (!TextUtils.stringSet(details.getArguments()))
        {
            details.setArguments(null);
        }

        if (!TextUtils.stringSet(details.getWorkingDir()))
        {
            details.setWorkingDir(null);
        }

        return details;
    }
}
