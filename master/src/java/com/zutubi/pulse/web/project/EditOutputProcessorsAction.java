package com.zutubi.pulse.web.project;

import com.zutubi.pulse.PostProcessorManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.TemplatePulseFileDetails;

import java.util.List;
import java.util.Map;

/**
 */
public class EditOutputProcessorsAction extends ProjectActionSupport
{
    private long projectId;
    private Project project;
    private Map<String, String> processorList;
    private List<String> processors;
    private PostProcessorManager postProcessorManager;

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

    public Map<String, String> getProcessorList()
    {
        if(processorList == null)
        {
            processorList = postProcessorManager.getAvailableProcessors();
        }
        return processorList;
    }

    public List<String> getProcessors()
    {
        return processors;
    }

    public void setProcessors(List<String> processors)
    {
        this.processors = processors;
    }

    public String doInput()
    {
        project = getProjectManager().getProject(projectId);
/*
        if(project != null && project.getPulseFileDetails().isBuiltIn())
        {
            TemplatePulseFileDetails details = (TemplatePulseFileDetails) project.getPulseFileDetails();
            processors = details.getOutputProcessors();
        }
*/
        return INPUT;
    }

    public void validate()
    {
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }
    }

    public String execute()
    {
/*
        TemplatePulseFileDetails details = (TemplatePulseFileDetails) project.getPulseFileDetails();
        List<String> procs = details.getOutputProcessors();
        procs.clear();
        if(processors != null)
        {
            procs.addAll(processors);
        }
*/

        getProjectManager().save(project);
        return SUCCESS;
    }

    public void setPostProcessorManager(PostProcessorManager postProcessorManager)
    {
        this.postProcessorManager = postProcessorManager;
    }
}
