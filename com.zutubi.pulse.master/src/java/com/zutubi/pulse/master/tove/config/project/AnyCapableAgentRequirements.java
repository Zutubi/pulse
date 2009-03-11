package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.engine.PulseFileSource;
import com.zutubi.pulse.master.AgentService;
import com.zutubi.pulse.master.RecipeAssignmentRequest;
import com.zutubi.pulse.master.scm.ScmFileResolver;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 * Requirements that allow a stage to be dispatched to any agent that has the
 * required resources.
 */
public class AnyCapableAgentRequirements implements AgentRequirements
{
    private static final Logger LOG = Logger.getLogger(AnyCapableAgentRequirements.class);

    private PulseFileLoaderFactory fileLoaderFactory;
    private ScmManager scmManager;

    public AnyCapableAgentRequirements()
    {
    }

    public String getSummary()
    {
        return "[any]";
    }

    public boolean fulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        List<ResourceRequirement> requirements = request.getResourceRequirements();
        for(ResourceRequirement requirement: requirements)
        {
            if(!service.hasResource(requirement))
            {
                return false;
            }
        }

        PulseFileLoader fileLoader = fileLoaderFactory.createLoader();
        try
        {
            BuildRevision buildRevision = request.getRevision();
            PulseFileSource pulseFile = buildRevision.getPulseFile();
            FileResolver fileResolver = new ScmFileResolver(request.getProject().getConfig(), buildRevision.getRevision(), scmManager);
            requirements = fileLoader.loadRequiredResources(pulseFile.getFileContent(), request.getRequest().getRecipeName(), new RelativeFileResolver(pulseFile.getPath(), fileResolver));
            for(ResourceRequirement requirement: requirements)
            {
                if(!service.hasResource(requirement))
                {
                    return false;
                }
            }
        }
        catch (PulseException e)
        {
            // Continue, assuming no further requirements.
            LOG.warning("Unable to load resource requirements from pulse file for project '" + request.getProject().getName() + "': " + e.getMessage(), e);
        }

        return true;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
