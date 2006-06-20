package com.zutubi.pulse.model;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.scm.SCMServer;

/**
 * A post build action that applies a tag to the built revision in the
 * repository.
 */
public class TagPostBuildAction extends PostBuildAction
{
    private String tag;
    private boolean moveExisting;

    protected void internalExecute(BuildResult result)
    {
        try
        {
            String tagName = substituteVariables(tag, result);
            SCMServer server = result.getProject().getScm().createServer();
            server.tag(result.getScmDetails().getRevision(), tagName, moveExisting);
        }
        catch (Exception e)
        {
            addError(e.getMessage());
        }
    }

    private String substituteVariables(String tag, BuildResult result) throws FileLoadException
    {
        Scope scope = new Scope();
        scope.add(new Property("project", result.getProject().getName()));
        scope.add(new Property("number", Long.toString(result.getNumber())));
        scope.add(new Property("status", result.getState().getString()));
        VariableHelper.replaceVariables(tag, true, scope);
        return tag;
    }

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
}
