package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.handler.FieldActionPredicate;
import com.zutubi.pulse.master.tove.model.FieldDescriptor;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.ProjectConfigProvider;
import com.zutubi.pulse.master.vfs.provider.pulse.RootFileObject;
import com.zutubi.tove.annotations.FieldAction;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.provider.UriParser;

import java.util.Collections;
import java.util.Set;

/**
 * Checks that a valid SCM exists and that the implementation supports
 * browsing before showing a browse action.
 */
public class ScmBrowsablePredicate implements FieldActionPredicate
{
    private static final Logger LOG = Logger.getLogger(ScmBrowsablePredicate.class);

    private FileSystemManager fileSystemManager;
    private ScmManager scmManager;
    private ProjectManager projectManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public boolean satisfied(FieldDescriptor field, FieldAction annotation)
    {
        String path = field.getPath();
        String projectPath;

        if (MasterConfigurationRegistry.PROJECTS_SCOPE.equals(PathUtils.getParentPath(path)))
        {
            if(StringUtils.stringSet(field.getBaseName()))
            {
                projectPath = RootFileObject.PREFIX_CONFIG + path;
            }
            else
            {
                projectPath = RootFileObject.NODE_WIZARDS + "/" + path;
            }
        }
        else
        {
            projectPath = RootFileObject.PREFIX_CONFIG + PathUtils.getPath(0, 2, PathUtils.getPathElements(path));
        }

        Set<ScmCapability> capabilities = Collections.emptySet();
        try
        {
            AbstractPulseFileObject pfo = (AbstractPulseFileObject) fileSystemManager.resolveFile("pulse:///" + UriParser.encode(projectPath));
            ProjectConfigProvider projectConfigProvider = (ProjectConfigProvider)pfo;
            if (projectConfigProvider != null && projectConfigProvider.getProjectConfig() != null)
            {
                ProjectConfiguration projectConfig = projectConfigProvider.getProjectConfig();
                ScmConfiguration config = projectConfig.getScm();
                if (config != null && configurationTemplateManager.isDeeplyCompleteAndValid(config))
                {
                    Project project = projectManager.getProject(projectConfig.getProjectId(), true);
                    if (project == null)
                    {
                        capabilities = ScmClientUtils.withScmClient(config, scmManager, new ScmClientUtils.ScmContextualAction<Set<ScmCapability>>()
                        {
                            public Set<ScmCapability> process(ScmClient scmClient, ScmContext context) throws ScmException
                            {
                                return scmClient.getCapabilities(context);
                            }
                        });
                    }
                    else
                    {
                        capabilities = ScmClientUtils.getCapabilities(projectConfig, project.getState(), scmManager);
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOG.warning(e);
        }

        return capabilities.contains(ScmCapability.BROWSE);
    }

    public void setFileSystemManager(FileSystemManager fileSystemManager)
    {
        this.fileSystemManager = fileSystemManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
