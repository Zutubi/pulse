package com.zutubi.pulse.master.hook.tag;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wire;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.ScmCapability;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.tove.config.project.hooks.*;
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

    private ScmClientFactory<ScmConfiguration> scmClientFactory;

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
            return;
        }

        ScmClient client = null;
        try
        {
            String tagName = VariableHelper.replaceVariables(tag, context.getScope(), VariableHelper.ResolutionStrategy.RESOLVE_STRICT);
            client = scmClientFactory.createClient(buildResult.getProject().getConfig().getScm());
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

    public void setScmClientFactory(ScmClientFactory<ScmConfiguration> scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}

