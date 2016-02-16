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
import com.zutubi.tove.annotations.FieldAction;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.ui.forms.FieldActionPredicate;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.model.forms.FieldModel;
import com.zutubi.util.logging.Logger;

import java.util.Collections;
import java.util.Set;

/**
 * Checks that a valid SCM exists and that the implementation supports
 * browsing before showing a browse action.
 */
public class ScmBrowsablePredicate implements FieldActionPredicate
{
    private static final Logger LOG = Logger.getLogger(ScmBrowsablePredicate.class);

    private ScmManager scmManager;
    private ProjectManager projectManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    @Override
    public boolean satisfied(FieldModel field, FieldAction annotation, FormContext context)
    {
        String closestExistingPath = context.getClosestExistingPath();
        if (closestExistingPath == null)
        {
            return false;
        }

        String[] elements = PathUtils.getPathElements(closestExistingPath);
        if (elements.length < 2)
        {
            return false;
        }

        String projectName = elements[1];
        return satisfied(projectName);
    }

    private boolean satisfied(String projectName)
    {
        Set<ScmCapability> capabilities = Collections.emptySet();
        try
        {
            ProjectConfiguration projectConfig = projectManager.getProjectConfig(projectName, false);
            if (projectConfig != null && projectConfig.getScm() != null)
            {
                ScmConfiguration config = projectConfig.getScm();
                Project project = projectManager.getProject(projectConfig.getProjectId(), false);
                if (project == null)
                {
                    // Template project.
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
        catch (Exception e)
        {
            LOG.warning(e);
        }

        return capabilities.contains(ScmCapability.BROWSE);
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
