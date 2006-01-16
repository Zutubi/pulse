package com.cinnamonbob.web.project;

import com.cinnamonbob.model.AntBobFileDetails;
import com.cinnamonbob.model.BobFileDetails;
import com.cinnamonbob.model.MavenBobFileDetails;

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
        return details;
    }
}
