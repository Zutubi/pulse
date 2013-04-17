package com.zutubi.pulse.acceptance.utils;

import com.google.common.base.Predicate;
import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.build.log.BuildLogFile;
import com.zutubi.pulse.master.build.log.LogFile;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupUnit;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import com.zutubi.pulse.master.cleanup.config.RetainConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.io.IsDirectoryPredicate;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;
import static com.zutubi.pulse.acceptance.Constants.Project.Cleanup.*;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.ARTIFACTS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.Artifact.POSTPROCESSORS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.DirectoryArtifact.BASE;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import static java.util.Arrays.asList;

/**
 * A set of utility methods used by the cleanup acceptance tests.
 */
public class CleanupTestUtils
{
    private RemoteApiClient remoteApi;

    public CleanupTestUtils(RemoteApiClient remoteApi)
    {
        this.remoteApi = remoteApi;
    }

    public void deleteCleanupRule(String projectName, String name) throws Exception
    {
        String cleanupPath = "projects/" + projectName + "/cleanup/" + name;
        remoteApi.deleteConfig(cleanupPath);
    }

    public void addCleanupRule(String projectName, String name, CleanupWhat... whats) throws Exception
    {
        Hashtable<String, Object> data = remoteApi.createDefaultConfig(CleanupConfiguration.class);
        data.put(NAME, name);
        data.put(RETAIN, 1);
        data.put(UNIT, CleanupUnit.BUILDS.toString());
        if (whats != null && whats.length > 0)
        {
            Vector<String> vector = new Vector<String>();
            for (CleanupWhat w : whats)
            {
                vector.add(w.toString());
            }
            data.put(WHAT, vector);
            data.put(CLEANUP_ALL, false);
        }
        else
        {
            data.put(CLEANUP_ALL, true);
        }

        String cleanupPath = "projects/" + projectName + "/cleanup";
        remoteApi.insertConfig(cleanupPath, data);
    }
    
    public void addRetainRule(String projectName, String name, ResultState... states) throws Exception
    {
        Hashtable<String, Object> data = remoteApi.createDefaultConfig(RetainConfiguration.class);
        data.put(NAME, name);
        data.put(RETAIN, 1);
        data.put(UNIT, CleanupUnit.BUILDS.toString());
        if (states != null && states.length > 0)
        {
            Vector<String> vector = new Vector<String>();
            for (ResultState s : states)
            {
                vector.add(s.toString());
            }
            data.put(STATES, vector);
        }

        String cleanupPath = "projects/" + projectName + "/cleanup";
        remoteApi.insertConfig(cleanupPath, data);
    }

    public boolean hasBuild(String projectName, int buildNumber) throws Exception
    {
        return remoteApi.getBuild(projectName, buildNumber) != null;
    }

    public File getBuildDirectory(String projectName, int buildNumber) throws Exception
    {
        Hashtable<String, Object> projectConfig = remoteApi.getProject(projectName);
        long projectId = Long.parseLong((String) projectConfig.get("id"));

        File data = AcceptanceTestUtils.getDataDirectory();

        return new File(data, "projects/" + projectId + "/" + String.format("%08d", buildNumber));
    }

    public boolean hasIvyFile(String projectName, int buildNumber) throws Exception
    {
        Repository repository = new Repository();
        String path = repository.getIvyModuleDescriptorPath(null, projectName, buildNumber);
        return repository.isInRepository(path);
    }

    public boolean hasBuildDirectory(String projectName, int buildNumber) throws Exception
    {
        return getBuildDirectory(projectName, buildNumber).isDirectory();
    }

    public boolean hasBuildLog(String projectName, int buildNumber) throws Exception
    {
        File buildDir = getBuildDirectory(projectName, buildNumber);
        return new LogFile(new File(buildDir, BuildLogFile.LOG_FILENAME), false).exists();
    }

    public boolean hasBuildOutputDirectory(String projectName, int buildNumber) throws Exception
    {
        return hasStageDirectory(projectName, buildNumber, "output");
    }

    public boolean hasBuildFeaturesDirectory(String projectName, int buildNumber) throws Exception
    {
        return hasStageDirectory(projectName, buildNumber, "features");
    }

    private boolean hasStageDirectory(String projectName, int buildNumber, final String directoryName) throws Exception
    {
        File buildDir = getBuildDirectory(projectName, buildNumber);

        Iterable<File> stageDirectories = filter(asList(buildDir.listFiles()), new IsDirectoryPredicate());

        return any(stageDirectories, new Predicate<File>()
        {
            public boolean apply(File file)
            {
                return new File(file, directoryName).isDirectory();
            }
        });
    }

    public void insertTestCapture(String projectPath, String processorName) throws Exception
    {
        Hashtable<String, Object> dirArtifactConfig = remoteApi.createDefaultConfig(DirectoryArtifactConfiguration.class);
        dirArtifactConfig.put(NAME, "xml reports");
        dirArtifactConfig.put(BASE, "build/reports/xml");
        dirArtifactConfig.put(POSTPROCESSORS, new Vector<String>(asList(PathUtils.getPath(projectPath, POSTPROCESSORS, processorName))));
        remoteApi.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), dirArtifactConfig);
    }

    public void setAntTarget(String projectName, String target) throws Exception
    {
        String path = "projects/" + projectName + "/type/recipes/default/commands/build";
        Hashtable<String, Object> antConfig = remoteApi.getConfig(path);
        antConfig.put(Constants.Project.AntCommand.TARGETS, target);
        remoteApi.saveConfig(path, antConfig, false);
    }
}
