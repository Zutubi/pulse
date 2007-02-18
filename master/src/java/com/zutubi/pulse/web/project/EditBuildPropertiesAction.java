package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.util.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EditBuildPropertiesAction extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(EditBuildPropertiesAction.class);

    private long id = -1;
    private Project project;
    private BuildSpecification spec;
    private String revision;
    private List<ResourceProperty> properties;
    private static final String PROPERTY_PREFIX = "property.";

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return project;
    }

    public BuildSpecification getSpec()
    {
        return spec;
    }

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

    public void validate()
    {
    }

    private void lookupSpec()
    {
        if(id > 0)
        {
            spec = project.getBuildSpecification(id);
        }
        else
        {
            spec = project.getDefaultSpecification();
        }

        if (spec == null)
        {
            addActionError("Request to build unknown build specification id [" + id + "] for project '" + project.getName() + "'");
        }
    }

    public String doInput() throws Exception
    {
        project = lookupProject(projectId);
        if(hasErrors())
        {
            return ERROR;
        }

        lookupSpec();
        if(hasErrors())
        {
            return ERROR;
        }

        properties = new ArrayList<ResourceProperty>(spec.getProperties());
        Collections.sort(properties, new NamedEntityComparator());
        return INPUT;
    }

    public String execute()
    {
        project = lookupProject(projectId);
        if (hasErrors())
        {
            return ERROR;
        }

        getProjectManager().checkWrite(project);

        lookupSpec();
        if(hasErrors())
        {
            return ERROR;
        }

        mapProperties();
        projectManager.save(project);

        Revision r = null;
        if(TextUtils.stringSet(revision))
        {
            try
            {
                r = project.getScm().createServer().getRevision(revision);
            }
            catch (SCMException e)
            {
                addFieldError("revision", "Unable to verify revision: " + e.getMessage());
                LOG.severe(e);
                return INPUT;
            }
        }
        
        projectManager.triggerBuild(project, spec.getName(), new ManualTriggerBuildReason((String)getPrinciple()), r, true);

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
                ResourceProperty property = spec.getProperty(propertyName);
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
