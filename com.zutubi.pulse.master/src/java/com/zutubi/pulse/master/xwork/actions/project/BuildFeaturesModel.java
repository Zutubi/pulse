package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Function;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * JSON data model for features of a given level from a build.  Only includes
 * entries for stages, commands etc that have features of the given level.
 */
public class BuildFeaturesModel
{

    private List<FeatureModel> features;
    private List<StageFeaturesModel> stages;

    public BuildFeaturesModel(BuildResult buildResult, Feature.Level level, Urls urls)
    {
        if (buildResult.hasDirectMessages(level))
        {
            features = newArrayList(transform(buildResult.getFeatures(level), new SimpleFeatureFunction()));
        }
        
        for (RecipeResultNode stageResult: buildResult.getStages())
        {
            if (stageResult.hasMessages(level))
            {
                if (stages == null)
                {
                    stages = new LinkedList<StageFeaturesModel>();
                }
                
                stages.add(new StageFeaturesModel(buildResult, stageResult, level, urls));
            }
        }
    }

    public List<FeatureModel> getFeatures()
    {
        return features;
    }

    public List<StageFeaturesModel> getStages()
    {
        return stages;
    }

    public static class StageFeaturesModel
    {
        private String name;
        private String recipeName;
        private String agentName;
        private boolean complete;
        private List<FeatureModel> features;
        private List<CommandFeaturesModel> commands;

        public StageFeaturesModel(BuildResult buildResult, RecipeResultNode stageResult, Feature.Level level, Urls urls)
        {
            RecipeResult recipeResult = stageResult.getResult();
            
            name = stageResult.getStageName();
            recipeName = recipeResult.getRecipeNameSafe();
            agentName = stageResult.getAgentNameSafe();
            complete = recipeResult.completed();
            
            if (recipeResult.hasDirectMessages(level))
            {
                features = newArrayList(transform(recipeResult.getFeatures(level), new SimpleFeatureFunction()));
            }
            
            for (CommandResult command: recipeResult.getCommandResults())
            {
                if (command.hasMessages(level))
                {
                    if (commands == null)
                    {
                        commands = new LinkedList<CommandFeaturesModel>();
                    }
                    
                    commands.add(new CommandFeaturesModel(buildResult, command, level, urls));
                }
            }
        }

        public String getName()
        {
            return name;
        }

        public String getRecipeName()
        {
            return recipeName;
        }

        public String getAgentName()
        {
            return agentName;
        }

        public boolean isComplete()
        {
            return complete;
        }

        public List<FeatureModel> getFeatures()
        {
            return features;
        }

        public List<CommandFeaturesModel> getCommands()
        {
            return commands;
        }
    }

    public static class CommandFeaturesModel
    {
        private String name;
        private List<FeatureModel> features;
        private String artifactsUrl;
        private List<ArtifactFeaturesModel> artifacts;

        public CommandFeaturesModel(BuildResult buildResult, CommandResult commandResult, Feature.Level level, Urls urls)
        {
            name = commandResult.getCommandName();
            if (commandResult.hasDirectMessages(level))
            {
                features = newArrayList(transform(commandResult.getFeatures(level), new SimpleFeatureFunction()));
            }
            
            artifactsUrl = urls.commandArtifacts(buildResult, commandResult);

            if (commandResult.hasArtifactMessages(level))
            {
                artifacts = new LinkedList<ArtifactFeaturesModel>();
                for (StoredArtifact artifact: commandResult.getArtifacts())
                {
                    if (artifact.hasMessages(level))
                    {
                        artifacts.add(new ArtifactFeaturesModel(artifact, level));
                    }
                }
            }
        }

        public String getName()
        {
            return name;
        }

        public List<FeatureModel> getFeatures()
        {
            return features;
        }

        public String getArtifactsUrl()
        {
            return artifactsUrl;
        }

        public List<ArtifactFeaturesModel> getArtifacts()
        {
            return artifacts;
        }
    }
    
    public static class ArtifactFeaturesModel
    {
        private String name;
        private List<FileFeaturesModel> files = new LinkedList<FileFeaturesModel>();

        public ArtifactFeaturesModel(StoredArtifact artifact, Feature.Level level)
        {
            name = artifact.getName();
            for (StoredFileArtifact file: artifact.getChildren())
            {
                if (file.hasMessages(level))
                {
                    files.add(new FileFeaturesModel(file, level));
                }
            }
        }

        public String getName()
        {
            return name;
        }

        public List<FileFeaturesModel> getFiles()
        {
            return files;
        }
    }

    public static class FileFeaturesModel
    {
        private static final int FEATURE_LIMIT = 100;
        
        private String path;
        private int featureCount;
        private List<FeatureModel> features = new LinkedList<FeatureModel>();

        public FileFeaturesModel(StoredFileArtifact fileArtifact, Feature.Level level)
        {
            this.path = fileArtifact.getPath();
            List<PersistentFeature> features = fileArtifact.getFeatures(level);
            featureCount = features.size();
            if (featureCount > FEATURE_LIMIT)
            {
                features = features.subList(0, FEATURE_LIMIT);
            }
            
            for (PersistentFeature feature: features)
            {
                if (feature instanceof PersistentPlainFeature && ((PersistentPlainFeature) feature).hasContext())
                {
                    this.features.add(new ContextFeatureModel((PersistentPlainFeature) feature));
                }
                else
                {
                    this.features.add(new SimpleFeatureModel(feature));
                }
            }
        }
        
        public String getPath()
        {
            return path;
        }

        public int getFeatureCount()
        {
            return featureCount;
        }

        public List<FeatureModel> getFeatures()
        {
            return features;
        }
    }
    
    public static abstract class FeatureModel
    {
    }

    public static class SimpleFeatureModel extends FeatureModel
    {
        private String summary;

        protected SimpleFeatureModel(PersistentFeature feature)
        {
            this.summary = feature.getSummary();
        }

        public String getSummary()
        {
            return summary;
        }
    }

    public static class ContextFeatureModel extends FeatureModel
    {
        private List<String> summaryLines;
        private int lineOffset;
        private long lineNumber;

        protected ContextFeatureModel(PersistentPlainFeature feature)
        {
            summaryLines = feature.getSummaryLines();
            lineOffset = feature.lineOffset();
            lineNumber = feature.getLineNumber();
        }

        public List<String> getSummaryLines()
        {
            return summaryLines;
        }

        public int getLineOffset()
        {
            return lineOffset;
        }

        public long getLineNumber()
        {
            return lineNumber;
        }
    }

    private static class SimpleFeatureFunction implements Function<PersistentFeature, FeatureModel>
    {
        public FeatureModel apply(PersistentFeature persistentFeature)
        {
            return new SimpleFeatureModel(persistentFeature);
        }
    }
}
