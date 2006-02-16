package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BobFileDetails;
import com.cinnamonbob.model.MakeBobFileDetails;
import com.opensymphony.util.TextUtils;

/**
 * Action for editing properties of a make project.
 */
public class EditMakeDetailsAction extends AbstractEditDetailsAction
{
    private MakeBobFileDetails details = new MakeBobFileDetails();

    public void prepare()
    {
        details = getBobFileDetailsDao().findByIdAndType(getId(), MakeBobFileDetails.class);
    }

    public BobFileDetails getDetails()
    {
        if (!TextUtils.stringSet(details.getMakefile()))
        {
            details.setMakefile(null);
        }

        if (!TextUtils.stringSet(details.getTargets()))
        {
            details.setTargets(null);
        }

        return details;
    }
}
