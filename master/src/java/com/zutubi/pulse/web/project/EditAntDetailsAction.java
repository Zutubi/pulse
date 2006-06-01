package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.AntPulseFileDetails;
import com.zutubi.pulse.model.PulseFileDetails;
import com.opensymphony.util.TextUtils;

/**
 * Action for editing properties of an ant project.
 */
public class EditAntDetailsAction extends AbstractEditDetailsAction
{
    private AntPulseFileDetails details = new AntPulseFileDetails();

    public void prepare()
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), AntPulseFileDetails.class);
    }

    public PulseFileDetails getDetails()
    {
        if (!TextUtils.stringSet(details.getBuildFile()))
        {
            details.setBuildFile(null);
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
