package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.util.Predicate;
import org.acegisecurity.acl.basic.AclObjectIdentity;
import org.acegisecurity.acl.basic.AclObjectIdentityAware;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class BuildResult extends Result implements AclObjectIdentityAware, Iterable<RecipeResultNode>
{
    public static final String BUILD_LOG = "build.log";
    public static final String PULSE_FILE = "pulse.xml";

    private BuildReason reason;
    private Project project;
    /**
     * If not null, this build is a personal build for the given user.
     */
    private User user;

    private long number;
    /**
     * If true, this build was triggered by a user at a fixed revision.
     * This build will be ignored for the purposes of calculating changes
     * between builds.
     */
    private boolean userRevision;
    private Revision revision;
    private RecipeResultNode root;
    private String version;
    /**
     * Set to false when the working directory is cleaned up.
     */
    private boolean hasWorkDir;

    private String status;

    public BuildResult()
    {

    }

    public BuildResult(BuildReason reason, Project project, long number, boolean userRevision)
    {
        // Clone the build reason to ensure that each build result has its own build reason. 
        try
        {
            this.reason = (BuildReason) reason.clone();
        }
        catch (CloneNotSupportedException e)
        {
            this.reason = reason;
        }

        this.project = project;
        this.user = null;
        this.number = number;
        this.userRevision = userRevision;
        state = ResultState.INITIAL;
        root = new RecipeResultNode(null, 0, null);
        hasWorkDir = true;
    }

    public BuildResult(BuildReason reason, User user, Project project, long number)
    {
        this(reason, project, number, false);
        this.user = user;
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

    public User getUser()
    {
        return user;
    }

    private void setUser(User user)
    {
        this.user = user;
    }

    public long getNumber()
    {
        return number;
    }

    private void setNumber(long number)
    {
        this.number = number;
    }

    public boolean isUserRevision()
    {
        return userRevision;
    }

    private void setUserRevision(boolean userRevision)
    {
        this.userRevision = userRevision;
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

    public Revision getRevision()
    {
        return revision;
    }

    public void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    String getRevisionString()
    {
        return revision == null ? null : revision.getRevisionString();
    }

    void setRevisionString(String revisionString)
    {
        if (revisionString == null)
        {
            revision = null;
        }
        else
        {
            revision = new Revision(revisionString);
        }
    }
    
    public AclObjectIdentity getAclObjectIdentity()
    {
        return project;
    }

    public RecipeResultNode findResultNode(final long id)
    {
        return root.findNode(new Predicate<RecipeResultNode>()
        {
            public boolean satisfied(RecipeResultNode recipeResultNode)
            {
                return recipeResultNode.getId() == id;
            }
        });
    }

    public RecipeResultNode findResultNodeByHandle(final long handle)
    {
        return root.findNode(new Predicate<RecipeResultNode>()
        {
            public boolean satisfied(RecipeResultNode recipeResultNode)
            {
                return recipeResultNode.getStageHandle() == handle;
            }
        });
    }

    public RecipeResultNode findResultNode(final String stageName)
    {
        return root.findNode(new Predicate<RecipeResultNode>()
        {
            public boolean satisfied(RecipeResultNode recipeResultNode)
            {
                return stageName.equals(recipeResultNode.getStageName());
            }
        });
    }

    public RecipeResultNode findResultNode(final CommandResult commandResult)
    {
        return root.findNode(new Predicate<RecipeResultNode>()
        {
            public boolean satisfied(RecipeResultNode recipeResultNode)
            {
                RecipeResult result = recipeResultNode.getResult();
                if (result != null)
                {
                    for (CommandResult command : result.getCommandResults())
                    {
                        if (command.equals(commandResult))
                        {
                            return true;
                        }
                    }
                }
                
                return false;
            }
        });
    }

    public StoredArtifact findArtifact(String artifactName)
    {
        for (RecipeResultNode child : root.getChildren())
        {
            StoredArtifact artifact = child.findArtifact(artifactName);
            if (artifact != null)
            {
                return artifact;
            }
        }
        return null;
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

    public boolean isPersonal()
    {
        return user != null;
    }

    public Entity getOwner()
    {
        if (user == null)
        {
            return project;
        }
        else
        {
            return user;
        }
    }

    public String getOwnerName()
    {
        if(isPersonal())
        {
            return "personal";
        }
        else
        {
            return project.getName();
        }
    }

    public void loadFeatures(File dataRoot)
    {
        root.loadFeatures(dataRoot);
    }

    public void loadFailedTestResults(File dataRoot, int limitPerRecipe)
    {
        root.loadFailedTestResults(dataRoot, limitPerRecipe);
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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

    public TestResultSummary getTestSummary()
    {
        return root.getTestSummary();
    }

    public boolean hasTests()
    {
        return getTestSummary().getTotal() > 0;
    }

    public boolean hasBrokenTests()
    {
        return getTestSummary().getBroken() > 0;
    }

    public void calculateFeatureCounts()
    {
        super.calculateFeatureCounts();

        // determine the feature counts for the attached node hierarchy.
        calculateNodeFeatureCounts(root);
    }

    private void calculateNodeFeatureCounts(RecipeResultNode node)
    {
        // extract the information from the current node.
        RecipeResult result = node.getResult();
        if (result != null)
        {
            result.calculateFeatureCounts();

            warningFeatureCount += result.getWarningFeatureCount();
            errorFeatureCount += result.getErrorFeatureCount();
        }

        // recurse to the child nodes.
        for (RecipeResultNode child : node.getChildren())
        {
            calculateNodeFeatureCounts(child);
        }
    }
}
