package com.zutubi.pulse.master.hook.tag;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wire;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.hooks.*;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.annotations.Required;

/**
 * A hook task that tags the built revision in the scm.
 */
@SymbolicName("zutubi.tagTaskConfig")
@Form(fieldOrder = {"tag", "moveExisting"})
@CompatibleHooks({ManualBuildHookConfiguration.class, PostBuildHookConfiguration.class, PostStageHookConfiguration.class})
@Wire
public class TagTaskConfiguration extends AbstractConfiguration implements BuildHookTaskConfiguration
{
    private static final Logger LOG = Logger.getLogger(TagTaskConfiguration.class);

    @Required
    private String tag;
    private boolean moveExisting;

    @Transient
    private ScmManager scmManager;

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public boolean isMoveExisting()
    {
        return moveExisting;
    }

    public void setMoveExisting(boolean moveExisting)
    {
        this.moveExisting = moveExisting;
    }

    public void execute(ExecutionContext context, BuildResult buildResult, RecipeResultNode resultNode) throws Exception
    {
        Revision revision = buildResult.getRevision();
        if(revision == null)
        {
            LOG.warning("Can not tag build result: No revision available.");
            return;
        }

        ScmConfiguration scm = buildResult.getProject().getConfig().getScm();
        if (!scmManager.isReady(scm))
        {
            LOG.warning("Can not tag build result: Scm is not ready.");
            return;
        }

        ScmClient client = null;
        try
        {
            String tagName = VariableHelper.replaceVariables(tag, context.getScope(), VariableHelper.ResolutionStrategy.RESOLVE_STRICT);
            client = scmManager.createClient(scm);
            if(client.getCapabilities().contains(ScmCapability.TAG))
            {
                client.tag(context, revision, tagName, moveExisting);
            }
            else
            {
                LOG.warning("Unable to run tag hook task for project '" + buildResult.getProject().getName() + "' as the SCM does not support tagging.");
            }
        }
        finally
        {
            ScmClientUtils.close(client);
        }
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}

