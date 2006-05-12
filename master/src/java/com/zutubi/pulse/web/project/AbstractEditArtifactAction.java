/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.Capture;
import com.zutubi.pulse.model.TemplatePulseFileDetails;
import com.zutubi.pulse.model.persistence.PulseFileDetailsDao;
import com.zutubi.pulse.xwork.interceptor.Preparable;
import com.zutubi.pulse.PostProcessorManager;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 *
 */
public abstract class AbstractEditArtifactAction extends ProjectActionSupport implements Preparable
{
    private static final List<String> PREPARE_PARAMS = Arrays.asList("id", "projectId");

    private long id;
    private long projectId;
    private Project project;
    private Capture capture;
    private List<String> processors;
    private Map<String, String> processorList;
    private PostProcessorManager postProcessorManager;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return project;
    }

    public Capture getCapture()
    {
        return capture;
    }

    public List<String> getProcessors()
    {
        if(processors == null)
        {
            processors = capture.getProcessors();
        }

        return processors;
    }

    public void setProcessors(List<String> processors)
    {
        this.processors = processors;
    }

    public Map<String, String> getProcessorList()
    {
        if(processorList == null)
        {
            processorList = postProcessorManager.getAvailableProcessors();
        }

        return processorList;
    }

    public List<String> getPrepareParameterNames()
    {
        return PREPARE_PARAMS;
    }

    public void validate()
    {
        if(project != null)
        {
            return;
        }

        project = getProjectManager().getProject(projectId);
        if(project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

        if(!project.getPulseFileDetails().isBuiltIn())
        {
            addActionError("Invalid operation for project");
        }
    }

    public void prepare() throws Exception
    {
        validate();
        if(hasErrors())
        {
            return;
        }

        capture = ((TemplatePulseFileDetails)project.getPulseFileDetails()).getCapture(id);
        if(capture == null)
        {
            addActionError("Unknown artifact [" + id + "]");
            return;
        }

        if(!verifyCapture(capture))
        {
            addActionError("Invalid operation for artifact");
        }

        processors = capture.getProcessors();
    }

    public String doInput() throws Exception
    {
        prepare();
        if(hasErrors())
        {
            return ERROR;
        }

        return INPUT;
    }

    public String execute()
    {
        List<String> procs = capture.getProcessors();
        procs.clear();
        if(processors != null)
        {
            procs.addAll(processors);
        }

        capture.clearFields();
        getProjectManager().save(project);
        return SUCCESS;
    }

    protected abstract boolean verifyCapture(Capture capture);

    public void setPostProcessorManager(PostProcessorManager postProcessorManager)
    {
        this.postProcessorManager = postProcessorManager;
    }
}
