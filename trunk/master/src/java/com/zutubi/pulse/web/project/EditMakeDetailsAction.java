package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.MakePulseFileDetails;
import com.opensymphony.util.TextUtils;

/**
 * Action for editing properties of a make project.
 */
public class EditMakeDetailsAction extends AbstractEditDetailsAction
{
    private MakePulseFileDetails details = new MakePulseFileDetails();

    public void prepare()
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), MakePulseFileDetails.class);
    }

    public PulseFileDetails getDetails()
    {
        if (!TextUtils.stringSet(details.getMakefile()))
        {
            details.setMakefile(null);
        }

        if (!TextUtils.stringSet(details.getTargets()))
        {
            details.setTargets(null);
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
