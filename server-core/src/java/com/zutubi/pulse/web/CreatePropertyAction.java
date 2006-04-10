package com.zutubi.pulse.web;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.persistence.ResourceVersionDao;

/**
 * 
 *
 */
public class CreatePropertyAction extends ActionSupport
{
    private long resourceId;
    private Long versionId;
    private Resource resource;
    private ResourceVersion version = null;
    private Property property = new Property();
    private ResourceDao resourceDao;
    private ResourceVersionDao resourceVersionDao;

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

    public Property getProperty()
    {
        return property;
    }

    public void setProperty(Property property)
    {
        this.property = property;
    }

    // The direct use of value is purely to get around the fact that Property
    // implements Reference, meaning its getValue() returns Object, which
    // appears to disagree with WebWork.
    public String getValue()
    {
        return property.getValue();
    }

    public void setValue(String value)
    {
        property.setValue(value);
    }

    public String doInput()
    {
        // setup any default data.
        return INPUT;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have 
            // completed successfully.
            return;
        }

        resource = resourceDao.findById(resourceId);
        if (resource == null)
        {
            addActionError("Unknown resource [" + resourceId + "]");
            return;
        }

        if (versionId != null)
        {
            version = resourceVersionDao.findById(versionId);
            // TODO: use same session!
            version = resource.getVersion(version.getValue());
        }

        if (version == null)
        {
            if (resource.hasProperty(property.getName()))
            {
                addFieldError("property.name", "this resource already contains a property with name '" + property.getName() + "'");
            }
        }
        else
        {
            if (version.hasProperty(property.getName()))
            {
                addFieldError("property.name", "this version already contains a property with name '" + property.getName() + "'");
            }
        }
    }

    public String execute()
    {
        try
        {
            if (version == null)
            {
                resource.addProperty(property);
            }
            else
            {
                version.addProperty(property);
            }
        }
        catch (FileLoadException e)
        {
            // TODO we ensure this does not happen, but still looks ugly
        }

        resourceDao.save(resource);
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
