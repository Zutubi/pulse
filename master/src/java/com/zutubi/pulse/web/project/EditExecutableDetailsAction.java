package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.AntPulseFileDetails;
import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.ExecutablePulseFileDetails;
import com.opensymphony.util.TextUtils;

/**
 * Action for editing properties of an executable project.
 */
public class EditExecutableDetailsAction extends AbstractEditDetailsAction
{
    private ExecutablePulseFileDetails details = new ExecutablePulseFileDetails();

    public void prepare()
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), ExecutablePulseFileDetails.class);
    }

    public PulseFileDetails getDetails()
    {
        if (!TextUtils.stringSet(details.getExecutable()))
        {
            details.setExecutable(null);
        }

        if (!TextUtils.stringSet(details.getArguments()))
        {
            details.setArguments(null);
        }

        if (!TextUtils.stringSet(details.getWorkingDir()))
        {
            details.setWorkingDir(null);
        }

        return details;
    }
}
