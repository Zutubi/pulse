package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BobFileDetails;
import com.cinnamonbob.model.CustomBobFileDetails;
import com.opensymphony.xwork.Preparable;

/**
 *
 *
 */
public class EditCustomDetailsAction extends AbstractEditDetailsAction implements Preparable
{
    private CustomBobFileDetails details = new CustomBobFileDetails();

    public void prepare()
    {
        details = getBobFileDetailsDao().findByIdAndType(getId(), CustomBobFileDetails.class);
    }

    public BobFileDetails getDetails()
    {
        return details;
    }
}
