package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.core.ResourceRepository;
import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.LinkedList;

/**
 */
public class BuildStageResourceActionSupport extends BuildStageActionSupport
{
    private ResourceRepository resourceRepository;
    protected ResourceRequirement resourceRequirement = new ResourceRequirement();
    private List<String> allResources;

    public ResourceRequirement getResourceRequirement()
    {
        return resourceRequirement;
    }

    public List<String> getAllResources()
    {
        if(allResources == null)
        {
            // TODO: dev-distributed: slave resource repos.
            allResources = new LinkedList<String>();
            allResources.add("");
            allResources.addAll(resourceRepository.getResourceNames());
        }

        return allResources;
    }

    public void setResourceRepository(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }

    protected void lookupNode()
    {
        node = getSpecification().getNode(getId());
        if(node == null)
        {
            node = getSpecification().getRoot();
        }
    }

    protected void addFieldsToResource()
    {
        if(!TextUtils.stringSet(resourceRequirement.getVersion()))
        {
            resourceRequirement.setVersion(null);
        }
    }
}
