package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.model.CustomPulseFileDetails;
import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.xwork.interceptor.Preparable;

/**
 *
 *
 */
public class EditCustomDetailsAction extends AbstractEditDetailsAction implements Preparable
{
    private CustomPulseFileDetails details = new CustomPulseFileDetails();
    private CustomDetailsHelper detailsHelper = new CustomDetailsHelper();
    private ResourceRepository resourceRepository;

    public void prepare()
    {
        details = getPulseFileDetailsDao().findByIdAndType(getId(), CustomPulseFileDetails.class);
    }

    public PulseFileDetails getDetails()
    {
        return details;
    }

    public CustomDetailsHelper getDetailsHelper()
    {
        return detailsHelper;
    }

    public void validate()
    {
        super.validate();
        if(hasErrors())
        {
            return;
        }

        detailsHelper.validate(this, details.getPulseFile(), resourceRepository);
    }

    public void setResourceRepository(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }
}
