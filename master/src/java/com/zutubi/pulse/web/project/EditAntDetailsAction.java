package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.AntBobFileDetails;
import com.zutubi.pulse.model.BobFileDetails;
import com.opensymphony.util.TextUtils;

/**
 * Action for editing properties of an ant project.
 */
public class EditAntDetailsAction extends AbstractEditDetailsAction
{
    private AntBobFileDetails details = new AntBobFileDetails();

    public void prepare()
    {
        details = getBobFileDetailsDao().findByIdAndType(getId(), AntBobFileDetails.class);
    }

    public BobFileDetails getDetails()
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
