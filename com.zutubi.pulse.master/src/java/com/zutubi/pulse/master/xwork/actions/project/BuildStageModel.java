package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.vfs.provider.pulse.FileAction;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines JSON data for a build stage.
 */
public class BuildStageModel extends ResultModel
{
    private static final Sort.StringComparator COMPARATOR = new Sort.StringComparator();

    private String name;
    private String recipe;
    private String agent;
    private String tests;
    private String buildLink;
    private List<ActionLink> featuredArtifacts;

    public BuildStageModel(BuildResult buildResult, RecipeResultNode stageResult, Urls urls, boolean collectArtifacts)
    {
        super(stageResult.getResult());
        name = stageResult.getStageName();
        RecipeResult recipeResult = stageResult.getResult();
        recipe = recipeResult.getRecipeName();
        agent = stageResult.getAgentName();
        tests = recipeResult.getTestSummary().toString();
        if (!buildResult.isPersonal() || buildResult.getUser().getLogin().equals(SecurityUtils.getLoggedInUsername()))
        {
            buildLink = Urls.getRelativeInstance().build(buildResult);
        }

        if (collectArtifacts)
        {
            addFeaturedArtifacts(buildResult, recipeResult, urls);
        }
    }

    private void addFeaturedArtifacts(final BuildResult buildResult, final RecipeResult recipeResult, final Urls urls)
    {
        if (recipeResult.completed())
        {
            List<ActionLink> collectedArtifacts = new LinkedList<ActionLink>();
            for (final CommandResult commandResult: recipeResult.getCommandResults())
            {
                Iterable<StoredArtifact> commandFeaturedArtifacts = Iterables.filter(commandResult.getArtifacts(), new Predicate<StoredArtifact>()
                {
                    public boolean apply(StoredArtifact storedArtifact)
                    {
                        return storedArtifact.isFeatured();
                    }
                });

                CollectionUtils.map(commandFeaturedArtifacts, new Mapping<StoredArtifact, ActionLink>()
                {
                    public ActionLink map(StoredArtifact artifact)
                    {
                        String icon;
                        String url;

                        if (artifact.isLink())
                        {
                            icon = FileAction.TYPE_LINK;
                            url = artifact.getUrl();
                        }
                        else if (artifact.isSingleFile())
                        {
                            StoredFileArtifact file = artifact.getFile();
                            if (file.canDecorate())
                            {
                                icon = FileAction.TYPE_DECORATE;
                                url = urls.commandArtifacts(buildResult, commandResult) + file.getPathUrl();
                            }
                            else
                            {
                                icon = FileAction.TYPE_DOWNLOAD;
                                url = urls.commandDownload(buildResult, commandResult, file.getPath());
                            }
                        }
                        else if (artifact.hasIndexFile())
                        {
                            icon = FileAction.TYPE_VIEW;
                            url = urls.fileFileArtifact(artifact, artifact.findFileBase(artifact.findIndexFile()));
                        }
                        else
                        {
                            icon = FileAction.TYPE_ARCHIVE;
                            url = urls.base() + "zip.action?path=pulse:///projects/" + buildResult.getProject().getId() + "/builds/" + buildResult.getId() + "/artifacts/" + recipeResult.getId() + "/" + commandResult.getId() + "/" + artifact.getId() + "/";
                        }

                        return new ActionLink(url, artifact.getName(), icon);
                    }
                }, collectedArtifacts);
            }

            if (collectedArtifacts.size() > 0)
            {
                Collections.sort(collectedArtifacts, new Comparator<ActionLink>()
                {
                    public int compare(ActionLink l1, ActionLink l2)
                    {
                        return COMPARATOR.compare(l1.getLabel(), l2.getLabel());
                    }
                });
                    
                this.featuredArtifacts = collectedArtifacts;
            }
        }
    }

    public String getName()
    {
        return name;
    }

    public String getRecipe()
    {
        return recipe;
    }

    public String getAgent()
    {
        return agent;
    }

    public String getTests()
    {
        return tests;
    }

    public String getLink()
    {
        return buildLink;
    }

    public List<ActionLink> getFeaturedArtifacts()
    {
        return featuredArtifacts;
    }
}
