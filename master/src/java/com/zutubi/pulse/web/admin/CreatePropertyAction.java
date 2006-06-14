package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.persistence.ResourceVersionDao;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.web.ActionSupport;
import org.hsqldb.persist.HsqlProperties;

/**
 * 
 *
 */
public class CreatePropertyAction extends ResourceActionSupport
{
    private Property property = new Property();

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
        lookupResource();
        if(hasErrors())
        {
            return ERROR;
        }
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

        if (version == null)
        {
            if (resource.hasProperty(property.getName()))
            {
                addFieldError("property.name", "This resource already contains a property with name '" + property.getName() + "'");
            }
        }
        else
        {
            if (version.hasProperty(property.getName()))
            {
                addFieldError("property.name", "This version already contains a property with name '" + property.getName() + "'");
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

        getResourceManager().save(resource);
        return SUCCESS;
    }
}
