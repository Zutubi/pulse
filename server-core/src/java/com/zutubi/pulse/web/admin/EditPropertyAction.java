package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.persistence.ResourceVersionDao;
import com.zutubi.pulse.web.ActionSupport;
import com.opensymphony.util.TextUtils;

/**
 */
public class EditPropertyAction extends ActionSupport
{
    private long resourceId;
    private Long versionId;
    private String name;
    private String newName;
    private String newValue;
    private Resource resource;
    private ResourceVersion version = null;
    private Property property;
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

    public Resource getResource()
    {
        return resource;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Property getProperty()
    {
        return property;
    }

    public String getNewName()
    {
        return newName;
    }

    public void setNewName(String newName)
    {
        this.newName = newName;
    }

    public String getNewValue()
    {
        return newValue;
    }

    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    private void lookupProperty()
    {
        resource = resourceDao.findById(resourceId);
        if(resource == null)
        {
            addActionError("Unknown resource [" + resourceId + "]");
            return;
        }

        if(versionId == null)
        {
            property = resource.getProperty(name);
        }
        else
        {
            version = resourceVersionDao.findById(versionId);
            if(version == null)
            {
                addActionError("Unknown version [" + versionId + "]");
                return;
            }

            property = version.getProperty(name);
        }

        if(property == null)
        {
            addActionError("Unknown property [" + name + "]");
        }
    }

    public String doInput()
    {
        lookupProperty();
        if(hasErrors())
        {
            return ERROR;
        }

        newName = property.getName();
        newValue = property.getValue();

        return INPUT;
    }

    public void validate()
    {
        lookupProperty();

        if (hasErrors())
        {
            return;
        }

        if(!TextUtils.stringSet(newName))
        {
            addFieldError("newName", "name is required");
            return;
        }

        if(!property.getName().equals(newName))
        {
            // The name has changed, ensure that the new name does not clash.
            if (version == null)
            {
                if (resource.hasProperty(newName))
                {
                    addFieldError("newName", "this resource already contains a property with name '" + newName + "'");
                }
            }
            else
            {
                if (version.hasProperty(newName))
                {
                    addFieldError("newName", "this version already contains a property with name '" + newName + "'");
                }
            }
        }
    }

    public String execute()
    {
        Property newProperty = new Property(newName, newValue);

        try
        {
            if (version == null)
            {
                resource.deleteProperty(name);
                resource.addProperty(newProperty);
                resourceDao.save(resource);
            }
            else
            {
                version.deleteProperty(name);
                version.addProperty(newProperty);
                resourceVersionDao.save(version);
            }
        }
        catch (FileLoadException e)
        {
            // TODO we ensure this does not happen, but still looks ugly
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
