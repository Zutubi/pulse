/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.CustomPulseFileDetails;
import com.zutubi.pulse.xwork.interceptor.Preparable;

/**
 *
 *
 */
public class EditCustomDetailsAction extends AbstractEditDetailsAction implements Preparable
{
    private CustomPulseFileDetails details = new CustomPulseFileDetails();

    public void prepare()
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), CustomPulseFileDetails.class);
    }

    public PulseFileDetails getDetails()
    {
        return details;
    }
}
