package com.zutubi.pulse.model;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.SCMServerUtils;

import java.util.List;

/**
 * A post build action that applies a tag to the built revision in the
 * repository.
 */
public class TagPostBuildAction extends PostBuildAction
{
    private String tag;
    private boolean moveExisting;

    public TagPostBuildAction()
    {
    }

    public TagPostBuildAction(String name, List<BuildSpecification> specifications, List<ResultState> states, boolean failOnError, String tag, boolean moveExisting)
    {
        super(name, specifications, states, failOnError);
        this.tag = tag;
        this.moveExisting = moveExisting;
    }

    protected void internalExecute(BuildResult result, RecipeResultNode recipe, List<ResourceProperty> properties)
    {
        SCMServer server = null;
        try
        {
            String tagName = substituteVariables(tag, result, recipe, properties);
            server = result.getProject().getScm().createServer();
            server.tag(result.getScmDetails().getRevision(), tagName, moveExisting);
        }
        catch (Exception e)
        {
            addError(e.getMessage());
        }
        finally
        {
            SCMServerUtils.close(server);
        }
    }

    public String getType()
    {
        return "apply tag";
    }

    public PostBuildAction copy()
    {
        TagPostBuildAction copy = new TagPostBuildAction();
        copyCommon(copy);
        copy.tag = tag;
        copy.moveExisting = moveExisting;

        return copy;
    }

    private String substituteVariables(String tag, BuildResult build, RecipeResultNode recipe, List<ResourceProperty> properties) throws FileLoadException
    {
        Scope scope = new Scope();
        scope.add(properties);

        scope.add(new Property("project", build.getProject().getName()));
        scope.add(new Property("number", Long.toString(build.getNumber())));
        scope.add(new Property("specification", build.getBuildSpecification()));

        // Build or stage
        if(recipe == null)
        {
            scope.add(new Property("status", build.getState().getString()));
        }
        else
        {
            scope.add(new Property("status", recipe.getResult().getState().getString()));
        }

        return VariableHelper.replaceVariables(tag, scope);
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public boolean getMoveExisting()
    {
        return moveExisting;
    }

    public void setMoveExisting(boolean moveExisting)
    {
        this.moveExisting = moveExisting;
    }
}
