package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.XCodePulseFileDetails;

/**
 * <class-comment/>
 */
public class EditXCodeDetailsAction extends AbstractEditDetailsAction
{
    private XCodePulseFileDetails details = new XCodePulseFileDetails();

    public PulseFileDetails getDetails()
    {
        return details;
    }

    public void prepare() throws Exception
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), XCodePulseFileDetails.class);
    }
}
