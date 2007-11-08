package com.zutubi.pulse.master.hook.tag;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.prototype.config.project.hooks.BuildHookTaskConfiguration;
import com.zutubi.pulse.prototype.config.project.hooks.CompatibleHooks;
import com.zutubi.pulse.prototype.config.project.hooks.ManualBuildHookConfiguration;
import com.zutubi.pulse.prototype.config.project.hooks.PostBuildHookConfiguration;

/**
 * A hook task that tags the built revision in the scm.
 */
@SymbolicName("zutubi.tagTaskConfig")
@Form(fieldOrder = {"tag", "moveExisting"})
@CompatibleHooks({ManualBuildHookConfiguration.class, PostBuildHookConfiguration.class})
public class TagTaskConfiguration extends AbstractConfiguration implements BuildHookTaskConfiguration
{
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
            String tagName = VariableHelper.replaceVariables(tag, context.asScope(), false);
            client = scmClientFactory.createClient(buildResult.getProject().getConfig().getScm());
            client.tag(revision, tagName, moveExisting);
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

