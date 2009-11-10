package com.zutubi.pulse.acceptance.cleanup;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.acceptance.Constants;
import static com.zutubi.pulse.acceptance.Constants.Project.Cleanup.*;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.ARTIFACTS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.Artifact.POSTPROCESSORS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.DirectoryArtifact.BASE;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.Options.RETAIN_WORKING_COPY;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import com.zutubi.pulse.acceptance.XmlRpcHelper;
import com.zutubi.pulse.acceptance.dependencies.Repository;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupUnit;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * A set of utility methods used by the cleanup acceptance tests.
 */
public class CleanupTestUtils
{
    private XmlRpcHelper xmlRpcHelper;

    public CleanupTestUtils(XmlRpcHelper xmlRpcHelper)
    {
        this.xmlRpcHelper = xmlRpcHelper;
    }

    public void setRetainWorkingCopy(String projectName, boolean b) throws Exception
    {
        String optionsPath = "projects/" + projectName + "/options";
        Hashtable<String, Object> data = xmlRpcHelper.getConfig(optionsPath);
        data.put(RETAIN_WORKING_COPY, b);
        xmlRpcHelper.saveConfig(optionsPath, data, false);
    }

    public void deleteCleanupRule(String projectName, String name) throws Exception
    {
        String cleanupPath = "projects/" + projectName + "/cleanup/" + name;
        xmlRpcHelper.deleteConfig(cleanupPath);
    }

    public void addCleanupRule(String projectName, String name, CleanupWhat... whats) throws Exception
    {
        Hashtable<String, Object> data = xmlRpcHelper.createDefaultConfig(CleanupConfiguration.class);
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
        xmlRpcHelper.insertConfig(cleanupPath, data);
    }
    
    public boolean hasBuild(String projectName, int buildNumber) throws Exception
    {
        return xmlRpcHelper.getBuild(projectName, buildNumber) != null;
    }

    public File getBuildDirectory(String projectName, int buildNumber) throws Exception
    {
        Hashtable<String, Object> projectConfig = xmlRpcHelper.getProject(projectName);
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
        return new File(buildDir, BuildResult.BUILD_LOG).isFile();
    }

    public boolean hasBuildWorkingCopy(String projectName, int buildNumber) throws Exception
    {
        return hasStageDirectory(projectName, buildNumber, "base");
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

        List<File> stageDirectories = CollectionUtils.filter(buildDir.listFiles(), new Predicate<File>()
        {
            public boolean satisfied(File file)
            {
                return file.isDirectory();
            }
        });

        return CollectionUtils.contains(stageDirectories, new Predicate<File>()
        {
            public boolean satisfied(File file)
            {
                return new File(file, directoryName).isDirectory();
            }
        });
    }

    public void insertTestCapture(String projectPath, String processorName) throws Exception
    {
        Hashtable<String, Object> dirArtifactConfig = xmlRpcHelper.createDefaultConfig(DirectoryArtifactConfiguration.class);
        dirArtifactConfig.put(NAME, "xml reports");
        dirArtifactConfig.put(BASE, "build/reports/xml");
        dirArtifactConfig.put(POSTPROCESSORS, new Vector<String>(Arrays.asList(PathUtils.getPath(projectPath, POSTPROCESSORS, processorName))));
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), dirArtifactConfig);
    }

    public void setAntTarget(String projectName, String target) throws Exception
    {
        String path = "projects/" + projectName + "/type/recipes/default/commands/build";
        Hashtable<String, Object> antConfig = xmlRpcHelper.getConfig(path);
        antConfig.put(Constants.Project.AntCommand.TARGETS, target);
        xmlRpcHelper.saveConfig(path, antConfig, false);
    }
}
