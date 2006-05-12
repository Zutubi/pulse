/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.PulseFileDetailsDao;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;

/**
 *
 *
 */
public abstract class AbstractEditDetailsAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private Project project;
    private PulseFileDetailsDao pulseFileDetailsDao;
    private static final List<String> PREPARE_PARAMS = Arrays.asList("id", "projectId");

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

    public PulseFileDetailsDao getPulseFileDetailsDao()
    {
        return pulseFileDetailsDao;
    }

    public void setPulseFileDetailsDao(PulseFileDetailsDao pulseFileDetailsDao)
    {
        this.pulseFileDetailsDao = pulseFileDetailsDao;
    }

    public List<String> getPrepareParameterNames()
    {
        return PREPARE_PARAMS;
    }

    public void validate()
    {
        project = getProjectManager().getProject(projectId);
        if(project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
        }
    }

    public String doInput() throws Exception
    {
        prepare();

        project = getProjectManager().getProject(projectId);

        if (getDetails() == null)
        {
            addActionError("Unknown project details '" + getId() + "'");
            return ERROR;
        }

        return INPUT;
    }

    public String execute()
    {
        if (getDetails() == null)
        {
            return INPUT;
        }

        getPulseFileDetailsDao().save(getDetails());
        return SUCCESS;
    }

    public abstract PulseFileDetails getDetails();
}
