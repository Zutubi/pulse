/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.persistence.ResourceVersionDao;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.web.ActionSupport;
import com.opensymphony.util.TextUtils;

/**
 */
public class EditPropertyAction extends ResourceActionSupport
{
    private String name;
    private String newName;
    private String newValue;
    private Property property;

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
        lookupResource();
        if(hasErrors())
        {
            return;
        }

        lookupVersion();
        if(hasErrors())
        {
            return;
        }

        if(version == null)
        {
            property = resource.getProperty(name);
        }
        else
        {
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
            }
            else
            {
                version.deleteProperty(name);
                version.addProperty(newProperty);
            }

            getResourceManager().save(resource);
        }
        catch (FileLoadException e)
        {
            // TODO we ensure this does not happen, but still looks ugly
        }

        return SUCCESS;
    }
}
