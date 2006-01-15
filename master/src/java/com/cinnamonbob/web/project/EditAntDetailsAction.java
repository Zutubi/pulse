package com.cinnamonbob.web.project;

import com.cinnamonbob.model.AntBobFileDetails;
import com.cinnamonbob.model.BobFileDetails;

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
        return details;
    }
}
