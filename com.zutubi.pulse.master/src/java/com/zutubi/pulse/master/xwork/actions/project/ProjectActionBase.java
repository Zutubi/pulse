package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.committransformers.CommitMessageSupport;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 */
public class ProjectActionBase extends ActionSupport
{
    private String projectName;
    private Project project;
    private long projectId;
    protected BuildManager buildManager;
    protected ConfigurationTemplateManager configurationTemplateManager;
    protected ObjectFactory objectFactory;

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public String getu_projectName()
    {
        return uriComponentEncode(projectName);
    }

    public String geth_projectName()
    {
        return htmlEncode(projectName);
    }
    
    public long getProjectId()
    {
        return projectId;
    }

    public Feature.Level getErrorLevel()
    {
        return Feature.Level.ERROR;
    }

    public Feature.Level getWarningLevel()
    {
        return Feature.Level.WARNING;
    }

    public Feature.Level getInfoLevel()
    {
        return Feature.Level.INFO;
    }
    
    public List<Feature.Level> getFeatureLevels()
    {
        List<Feature.Level> list = Arrays.asList(Feature.Level.values());
        Collections.reverse(list);
        return list;
    }
    
    public Project getProject()
    {
        if(project == null)
        {
            if (StringUtils.stringSet(projectName))
            {
                project = projectManager.getProject(projectName, true);
                checkProject();
                projectId = project.getId();
            }
            else if (projectId > 0)
            {
                project = projectManager.getProject(projectId, true);
                checkProject();
                projectName = project.getName();
            }
        }

        return project;
    }

    private void checkProject()
    {
        if (project == null)
        {
            throw new LookupErrorException("Unknown project '" + projectName + "'");
        }
        if(!configurationTemplateManager.isDeeplyValid(project.getConfig().getConfigurationPath()))
        {
            throw new LookupErrorException("Project configuration is invalid.");
        }
    }

    public Project getRequiredProject()
    {
        Project project = getProject();
        if(project == null)
        {
            throw new LookupErrorException("Project name is required");
        }

        return project;
    }

    public CommitMessageSupport getCommitMessageSupport(PersistentChangelist changelist)
    {
        // When in the context of a project, only apply its own transformers
        return new CommitMessageSupport(changelist.getComment(), getProject().getConfig().getCommitMessageTransformers().values());
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
