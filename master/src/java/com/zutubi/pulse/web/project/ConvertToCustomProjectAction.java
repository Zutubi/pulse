package com.zutubi.pulse.web.project;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.model.CustomPulseFileDetails;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.util.XMLUtils;

/**
 */
public class ConvertToCustomProjectAction extends ProjectActionSupport
{
    private long id;
    private CustomPulseFileDetails details = new CustomPulseFileDetails();
    private CustomDetailsHelper detailsHelper = new CustomDetailsHelper();
    private Project project;
    private ResourceRepository resourceRepository;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public CustomPulseFileDetails getDetails()
    {
        return details;
    }

    public CustomDetailsHelper getDetailsHelper()
    {
        return detailsHelper;
    }

    public Project getProject()
    {
        return project;
    }

    public String doInput()
    {
        project = lookupProject(id);
        if(hasErrors())
        {
            return ERROR;
        }

        PulseFileDetails pulseFileDetails = project.getPulseFileDetails();
        ComponentContext.autowire(pulseFileDetails);
        String pulseFile = pulseFileDetails.getPulseFile(0, project, null, null);
        details.setPulseFile(XMLUtils.prettyPrint(pulseFile));

        return INPUT;
    }

    public void validate()
    {
        project = lookupProject(id);
        if(hasErrors())
        {
            return;
        }

        detailsHelper.validate(this, details.getPulseFile(), resourceRepository);
    }

    public String execute()
    {
        project.setPulseFileDetails(details);
        getProjectManager().save(project);

        return SUCCESS;
    }

    public void setResourceRepository(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }
}
