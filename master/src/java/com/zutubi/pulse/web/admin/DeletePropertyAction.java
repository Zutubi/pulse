/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.persistence.ResourceVersionDao;
import com.zutubi.pulse.web.ActionSupport;

/**
 * <class-comment/>
 */
public class DeletePropertyAction extends ActionSupport
{
    private ResourceDao resourceDao;
    private ResourceVersionDao resourceVersionDao;
    private long resourceId;
    private Long versionId;
    private String name;
    private Resource resource;
    private ResourceVersion resourceVersion;
    private Property property;

    public long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(long resourceId)
    {
        this.resourceId = resourceId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        if (versionId == null)
        {
            resource = resourceDao.findById(resourceId);
            if (resource == null)
            {
                addActionError("Unknown resource '" + resourceId + "'");
                return;
            }

            property = resource.getProperty(name);
        }
        else
        {
            resourceVersion = resourceVersionDao.findById(versionId);
            if (resourceVersion == null)
            {
                addActionError("Unknown resource version '" + versionId + "'");
                return;
            }

            property = resourceVersion.getProperty(name);
        }

        if (property == null)
        {
            addActionError("Unknown property '" + name + "'");
        }
    }

    public String execute()
    {
        if (resourceVersion == null)
        {
            resource.deleteProperty(name);
            resourceDao.save(resource);
        }
        else
        {
            resourceVersion.deleteProperty(name);
            resourceVersionDao.save(resourceVersion);
        }

        return SUCCESS;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }

    public void setResourceVersionDao(ResourceVersionDao resourceVersionDao)
    {
        this.resourceVersionDao = resourceVersionDao;
    }
}
