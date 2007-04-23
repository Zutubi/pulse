package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.config.NamedConfigurationComparator;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.util.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EditBuildPropertiesAction extends ProjectActionBase
{
    private static final Logger LOG = Logger.getLogger(EditBuildPropertiesAction.class);

    private static final String PROPERTY_PREFIX = "property.";

    private String revision;
    private List<ResourceProperty> properties;

    public List<ResourceProperty> getProperties()
    {
        return properties;
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
    }

    public String doInput() throws Exception
    {
        properties = new ArrayList<ResourceProperty>(getProjectConfig().getProperties().values());
        Collections.sort(properties, new NamedConfigurationComparator());
        return INPUT;
    }

    public String execute()
    {
        getProjectManager().checkWrite(getProject());

        mapProperties();
        projectManager.saveProjectConfig(getProjectConfig());

        Revision r = null;
        if(TextUtils.stringSet(revision))
        {
            try
            {
                r = getProjectConfig().getScm().createClient().getRevision(revision);
            }
            catch (ScmException e)
            {
                addFieldError("revision", "Unable to verify revision: " + e.getMessage());
                LOG.severe(e);
                return INPUT;
            }
        }
        
        projectManager.triggerBuild(getProject(), null, new ManualTriggerBuildReason((String)getPrinciple()), r, true);

        try
        {
            // Pause for dramatic effect
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // Empty
        }

        return SUCCESS;
    }

    private void mapProperties()
    {
        Map parameters = ActionContext.getContext().getParameters();
        for(Object n: parameters.keySet())
        {
            String name = (String) n;
            if(name.startsWith(PROPERTY_PREFIX))
            {
                String propertyName = name.substring(PROPERTY_PREFIX.length());
                ResourceProperty property = getProjectConfig().getProperty(propertyName);
                if(property != null)
                {
                    Object value = parameters.get(name);
                    if(value instanceof String)
                    {
                        property.setValue((String) value);
                    }
                    else if(value instanceof String[])
                    {
                        property.setValue(((String[])value)[0]);
                    }
                }
            }
        }
    }
}
