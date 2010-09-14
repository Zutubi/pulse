package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import org.springframework.security.acl.basic.AclObjectIdentity;
import org.springframework.security.acl.basic.AclObjectIdentityAware;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class BuildResult extends Result implements AclObjectIdentityAware, Iterable<RecipeResultNode>
{
    public static final String ACTION_ADD_COMMENT = "addComment";
    public static final String ACTION_CANCEL = "cancel";

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
    
    private String status;

    /**
     * A globally unique id used by a set of related builds to identify them as being related.
     */
    private long metaBuildId;

    /**
     * A list of build results that this build depends on.  That is, these are the build
     * results that needed to be completed before this result's build could commence.
     */
    private List<BuildResult> dependsOn =  new LinkedList<BuildResult>();
    /**
     * Descriptive comments left by users on this build.
     */
    private List<Comment> comments = new LinkedList<Comment>();

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
        state = ResultState.PENDING;
        root = new RecipeResultNode(null, 0, null);
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

    public long getMetaBuildId()
    {
        return metaBuildId;
    }

    public void setMetaBuildId(long metaBuildId)
    {
        this.metaBuildId = metaBuildId;
    }

    public List<BuildResult> getDependsOn()
    {
        return dependsOn;
    }

    public void setDependsOn(List<BuildResult> dependsOn)
    {
        this.dependsOn = dependsOn;
    }

    public void addDependsOn(BuildResult result)
    {
        this.dependsOn.add(result);
    }

    public List<Comment> getComments()
    {
        return comments;
    }

    public void setComments(List<Comment> comments)
    {
        this.comments = comments;
    }

    public void addComment(Comment comment)
    {
        comments.add(comment);
    }

    public boolean removeComment(Comment comment)
    {
        return comments.remove(comment);
    }

    /**
     * Get the build result (from the list of builds that this build depends on) that
     * is associated with the specified project.
     *
     * @param projectName   the name of the project build of interest.
     *
     * @return a build result or null if non is found.
     */
    public BuildResult getDependsOn(final String projectName)
    {
        return CollectionUtils.find(this.dependsOn, new Predicate<BuildResult>()
        {
            public boolean satisfied(BuildResult buildResult)
            {
                ProjectConfiguration dependsOnProject = buildResult.getProject().getConfig();
                return (dependsOnProject.getName().equals(projectName));
            }
        });
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

    /**
     * Finds the recipe result node holding the recipe with the given id, if
     * any.
     *
     * @param recipeId the recipe id to search for
     * @return the result node holding the given recipe, or null if not found
     */
    public RecipeResultNode findResultNodeByRecipeId(final long recipeId)
    {
        return root.findNode(new Predicate<RecipeResultNode>()
        {
            public boolean satisfied(RecipeResultNode recipeResultNode)
            {
                RecipeResult recipeResult = recipeResultNode.getResult();
                return recipeResult != null && recipeResult.getId() == recipeId;
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
        super.complete();

        // Check the recipe results, if there are any failures/errors
        // then take on the worst result from ourself or any recipe.
        state = ResultState.getWorseState(state, root.getWorstState(state));
    }

    public Iterator<RecipeResultNode> iterator()
    {
        return new ResultIterator();
    }

    public boolean isPersonal()
    {
        return user != null;
    }

    public NamedEntity getOwner()
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
