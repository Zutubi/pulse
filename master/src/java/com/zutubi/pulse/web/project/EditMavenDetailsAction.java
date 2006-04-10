package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BobFileDetails;
import com.zutubi.pulse.model.MavenBobFileDetails;
import com.opensymphony.util.TextUtils;

/**
 *
 *
 */
public class EditMavenDetailsAction extends AbstractEditDetailsAction
{
    private MavenBobFileDetails details = new MavenBobFileDetails();

    public void prepare()
    {
        details = getBobFileDetailsDao().findByIdAndType(getId(), MavenBobFileDetails.class);
    }

    public BobFileDetails getDetails()
    {
        if (!TextUtils.stringSet(details.getWorkingDir()))
        {
            details.setWorkingDir(null);
        }

        if (!TextUtils.stringSet(details.getTargets()))
        {
            details.setTargets(null);
        }

        if (!TextUtils.stringSet(details.getArguments()))
        {
            details.setArguments(null);
        }

        return details;
    }
}
