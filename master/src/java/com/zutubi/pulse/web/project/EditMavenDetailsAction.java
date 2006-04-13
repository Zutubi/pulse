/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.MavenPulseFileDetails;
import com.opensymphony.util.TextUtils;

/**
 *
 *
 */
public class EditMavenDetailsAction extends AbstractEditDetailsAction
{
    private MavenPulseFileDetails details = new MavenPulseFileDetails();

    public void prepare()
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), MavenPulseFileDetails.class);
    }

    public PulseFileDetails getDetails()
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
