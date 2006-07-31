package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.model.TestResultSummary;
import org.acegisecurity.acl.basic.AclObjectIdentity;
import org.acegisecurity.acl.basic.AclObjectIdentityAware;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class BuildResult extends Result implements AclObjectIdentityAware, Iterable<RecipeResultNode>
{
    public static final String PULSE_FILE = "pulse.xml";

    private BuildReason reason;
    private Project project;
    private String buildSpecification;
    private long number;
    private BuildScmDetails scmDetails;
    private RecipeResultNode root;
    /**
     * Set to false when the working directory is cleaned up.
     */
    private boolean hasWorkDir;

    public BuildResult()
    {

    }

    public BuildResult(BuildReason reason, Project project, String buildSpecification, long number)
    {
        this.reason = reason;
        this.project = project;
        this.buildSpecification = buildSpecification;
        this.number = number;
        state = ResultState.INITIAL;
        root = new RecipeResultNode(null, null);
        hasWorkDir = true;
    }

    public BuildReason getReason()
    {
        return reason;
    }

    private void setReason(BuildReason reason)
    {
        this.reason = reason;
    }

    public Project getProject()
    {
        return project;
    }

    private void setProject(Project project)
    {
        this.project = project;
    }

    public String getBuildSpecification()
    {
        return buildSpecification;
    }

    private void setBuildSpecification(String buildSpecification)
    {
        this.buildSpecification = buildSpecification;
    }

    public long getNumber()
    {
        return number;
    }

    private void setNumber(long number)
    {
        this.number = number;
    }

    public RecipeResultNode getRoot()
    {
        return root;
    }

    private void setRoot(RecipeResultNode root)
    {
        this.root = root;
    }

    public boolean getHasWorkDir()
    {
        return hasWorkDir;
    }

    public void setHasWorkDir(boolean hasWorkDir)
    {
        this.hasWorkDir = hasWorkDir;
    }

    public void abortUnfinishedRecipes()
    {
        for (RecipeResultNode node : root.getChildren())
        {
            node.abort();
        }
    }

    public List<String> collectErrors()
    {
        List<String> errors = super.collectErrors();

        for (RecipeResultNode node : root.getChildren())
        {
            errors.addAll(node.collectErrors());
        }

        return errors;
    }

    public boolean hasMessages(Feature.Level level)
    {
        if (hasDirectMessages(level))
        {
            return true;
        }

        for (RecipeResultNode node : root.getChildren())
        {
            if (node.hasMessages(level))
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasArtifacts()
    {
        for (RecipeResultNode node : root.getChildren())
        {
            if (node.hasArtifacts())
            {
                return true;
            }
        }

        return false;
    }

    public BuildScmDetails getScmDetails()
    {
        return scmDetails;
    }

    public void setScmDetails(BuildScmDetails scmDetails)
    {
        this.scmDetails = scmDetails;
    }

    public void accumulateTestSummary(TestResultSummary summary)
    {
        root.accumulateTestSummary(summary);
    }

    public AclObjectIdentity getAclObjectIdentity()
    {
        return project;
    }

    public RecipeResultNode findResultNode(long id)
    {
        return root.findNode(id);
    }

    public RecipeResultNode findResultNode(String stage)
    {
        return root.findNode(stage);
    }

    public void complete()
    {
        // Check the recipe results, if there are any failures/errors
        // then take on the worst result.
        state = root.getWorstState(state);

        super.complete();
    }

    public Iterator<RecipeResultNode> iterator()
    {
        return new ResultIterator();
    }

    private class ResultIterator implements Iterator<RecipeResultNode>
    {
        List<RecipeResultNode> remaining;

        public ResultIterator()
        {
            remaining = new LinkedList<RecipeResultNode>(root.getChildren());
        }

        public boolean hasNext()
        {
            return remaining.size() > 0;
        }

        public RecipeResultNode next()
        {
            RecipeResultNode next = remaining.remove(0);
            remaining.addAll(next.getChildren());
            return next;
        }

        public void remove()
        {
            throw new UnsupportedOperationException("Build result may not have recipes removed");
        }
    }
}
