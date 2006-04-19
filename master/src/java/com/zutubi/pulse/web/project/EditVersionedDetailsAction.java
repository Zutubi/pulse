/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.CustomPulseFileDetails;
import com.zutubi.pulse.model.VersionedPulseFileDetails;
import com.zutubi.pulse.xwork.interceptor.Preparable;

/**
 *
 *
 */
public class EditVersionedDetailsAction extends AbstractEditDetailsAction implements Preparable
{
    private VersionedPulseFileDetails details = new VersionedPulseFileDetails();

    public void prepare()
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), VersionedPulseFileDetails.class);
    }

    public PulseFileDetails getDetails()
    {
        return details;
    }
}
