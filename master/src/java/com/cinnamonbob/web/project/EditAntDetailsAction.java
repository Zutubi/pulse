package com.cinnamonbob.web.project;

import com.cinnamonbob.model.AntBobFileDetails;
import com.cinnamonbob.model.BobFileDetails;
import com.opensymphony.util.TextUtils;

/**
 *
 *
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

        return details;
    }
}
