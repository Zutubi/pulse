package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BobFileDetails;
import com.zutubi.pulse.model.CustomBobFileDetails;
import com.zutubi.pulse.xwork.interceptor.Preparable;

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
