/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.UnaryProcedure;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Iterables.find;

/**
 */
public class BuildResult extends Result implements Iterable<RecipeResultNode>, CommentContainer
{
    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_KILL = "kill";
    public static final String ACTION_PIN = "pin";
    public static final String ACTION_UNPIN = "unpin";

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
    private List<RecipeResultNode> stages;
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
    /**
     * Pinned builds cannot be deleted and are immune from cleanup.
     * They may be unpinned, though, and then deleted.
     */
    private boolean pinned;

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
        this.state = ResultState.PENDING;
        stages = new LinkedList<RecipeResultNode>();
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

    public List<RecipeResultNode> getStages()
    {
        return stages;
    }

    public void setStages(List<RecipeResultNode> stages)
    {
        this.stages = stages;
    }

    public void addStage(RecipeResultNode stage)
    {
        stages.add(stage);
    }

    public void removeStage(RecipeResultNode stage)
    {
        stages.remove(stage);
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

    public boolean isPinned()
    {
        return pinned;
    }

    public void setPinned(boolean pinned)
    {
        this.pinned = pinned;
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
        return find(this.dependsOn, new Predicate<BuildResult>()
        {
            public boolean apply(BuildResult buildResult)
            {
                ProjectConfiguration dependsOnProject = buildResult.getProject().getConfig();
                return (dependsOnProject.getName().equals(projectName));
            }
        }, null);
    }

    public void abortUnfinishedRecipes()
    {
        for (RecipeResultNode node : stages)
        {
            node.abort();
        }
    }

    public List<String> collectErrors()
    {
        List<String> errors = super.collectErrors();

        for (RecipeResultNode node : stages)
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

        for (RecipeResultNode node : stages)
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
        for (RecipeResultNode node : stages)
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
    
    public RecipeResultNode findResultNode(final long id)
    {
        return find(stages, new EntityWithIdPredicate<RecipeResultNode>(id), null);
    }

    public RecipeResultNode findResultNodeByHandle(final long handle)
    {
        return find(stages, new Predicate<RecipeResultNode>()
        {
            public boolean apply(RecipeResultNode recipeResultNode)
            {
                return recipeResultNode.getStageHandle() == handle;
            }
        }, null);
    }

    public RecipeResultNode findResultNode(final String stageName)
    {
        return find(stages, new Predicate<RecipeResultNode>()
        {
            public boolean apply(RecipeResultNode recipeResultNode)
            {
                return stageName.equals(recipeResultNode.getStageName());
            }
        }, null);
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
        return find(stages, new Predicate<RecipeResultNode>()
        {
            public boolean apply(RecipeResultNode recipeResultNode)
            {
                return recipeResultNode.getResult().getId() == recipeId;
            }
        }, null);
    }

    public RecipeResultNode findResultNode(final CommandResult commandResult)
    {
        return find(stages, new Predicate<RecipeResultNode>()
        {
            public boolean apply(RecipeResultNode recipeResultNode)
            {
                RecipeResult result = recipeResultNode.getResult();
                for (CommandResult command : result.getCommandResults())
                {
                    if (command.equals(commandResult))
                    {
                        return true;
                    }
                }

                return false;
            }
        }, null);
    }

    public StoredArtifact findArtifact(String artifactName)
    {
        for (RecipeResultNode child : stages)
        {
            StoredArtifact artifact = child.findArtifact(artifactName);
            if (artifact != null)
            {
                return artifact;
            }
        }
        return null;
    }

    public ResultState getWorstStageState()
    {
        ResultState state = ResultState.SUCCESS;
        for (RecipeResultNode stage: stages)
        {
            state = ResultState.getWorseState(state, stage.getResult().getState());
        }

        return state;
    }

    public void complete()
    {
        super.complete();

        // Check the stage results, if there are any failures/errors
        // then take on the worst result from ourselves or any stage.
        state = ResultState.getWorseState(state, getWorstStageState());
    }

    public Iterator<RecipeResultNode> iterator()
    {
        return stages.iterator();
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
        else if (project != null && project.getName() != null)
        {
            return project.getName();
        }
        else
        {
            return "<unknown>";
        }
    }

    public void loadFeatures(File dataRoot)
    {
        for (RecipeResultNode stage: stages)
        {
            stage.loadFeatures(dataRoot);
        }
    }

    public void loadFailedTestResults(File dataRoot, int limitPerRecipe)
    {
        for (RecipeResultNode stage: stages)
        {
            stage.loadFailedTestResults(dataRoot, limitPerRecipe);
        }
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

    public TestResultSummary getTestSummary()
    {
        TestResultSummary summary = new TestResultSummary();
        for (RecipeResultNode stage: stages)
        {
            stage.getResult().accumulateTestSummary(summary);
        }

        return summary;
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

        for (RecipeResultNode stage: stages)
        {
            RecipeResult result = stage.getResult();
            result.calculateFeatureCounts();
            warningFeatureCount += result.getWarningFeatureCount();
            errorFeatureCount += result.getErrorFeatureCount();
        }
    }

    public void forEachNode(UnaryProcedure<RecipeResultNode> fn)
    {
        for (RecipeResultNode stage: stages)
        {
            fn.run(stage);
        }
    }

    @Override
    public String toString()
    {
        return getOwnerName() + " :: build " + number; 
    }

    /**
     * Comparator to sort builds by owner then number.
     */
    public static class CompareByOwnerThenNumber implements Comparator<BuildResult>
    {
        private NamedEntityComparator nameComparator = new NamedEntityComparator();

        public int compare(BuildResult b1, BuildResult b2)
        {
            int result = nameComparator.compare(b1.getOwner(), b2.getOwner());
            if (result == 0)
            {
                result = (int) (b1.getNumber() - b2.getNumber());
            }

            return result;
        }
    }

    /**
     * Comparator to sort builds by their start time.
     */
    public static class ByStartTimeComparator implements Comparator<BuildResult>
    {
        public int compare(BuildResult b1, BuildResult b2)
        {
            return (int) (b1.getStamps().getStartTime() - b2.getStamps().getStartTime());
        }
    }
}
