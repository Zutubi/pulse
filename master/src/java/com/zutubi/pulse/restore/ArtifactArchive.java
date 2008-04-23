package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.MasterUserPaths;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.restore.feedback.Feedback;
import com.zutubi.pulse.restore.feedback.FeedbackProvider;
import com.zutubi.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class ArtifactArchive extends AbstractArchivableComponent implements FeedbackProvider
{
    private MasterUserPaths paths;
    private Feedback feedback;
    private SystemConfiguration systemConfiguration;

    public String getName()
    {
        return "artifacts";
    }

    public String getDescription()
    {
        return "The artifacts restoration consists of moving around the directories located within the " +
                "PULSE_DATA/projects directory to match the restructured project hierarchy.";
    }

    public void backup(File archive) throws ArchiveException
    {

    }

    public void restore(File archive) throws ArchiveException
    {
        // load the mappings file.
        try
        {
            File mappingsFile = new File(archive, "mappings.txt");
            Map<Long, Long> mappings = readMappings(mappingsFile);

            // progress on this restoration is based on how quickly and how many directories
            // we need to move/rename.  This is based purely on the number of build directories,
            // not the number of mappings.

            File base = paths.getProjectRoot();

            if (systemConfiguration.getRestoreArtifacts() != null)
            {
                base = new File(systemConfiguration.getRestoreArtifacts());
                if (!base.isDirectory())
                {
                    throw new ArchiveException("Requested artifact restore path " + base.getCanonicalPath() + " does not exist.");
                }
            }

            if (base.isDirectory())
            {
                // a) get a quick overall directory count.
                NonDeadDirectoryFilter directoriesOnly = new NonDeadDirectoryFilter();

                long todoCount = 0;
                for (File projectDir : base.listFiles(directoriesOnly))
                {
                    for (File buildDir : projectDir.listFiles(directoriesOnly))
                    {
                        if (buildDir.getName().equals("builds"))
                        {
                            todoCount = todoCount + buildDir.listFiles(directoriesOnly).length;
                        }
                        else
                        {
                            todoCount++;
                        }
                    }
                }

                long completedCout = 0;
                for (File projectDir : base.listFiles(directoriesOnly))
                {
                    for (File buildDir : projectDir.listFiles(directoriesOnly))
                    {
                        if (buildDir.getName().equals("builds"))
                        {
                            for (File nestedBuildDir : buildDir.listFiles(directoriesOnly))
                            {
                                processBuildDirectory(nestedBuildDir, directoriesOnly, mappings, base);
                                completedCout++;
                                feedback.setPercetageComplete((int) (completedCout * 100 / todoCount));
                            }
                        }
                        else
                        {
                            processBuildDirectory(buildDir, directoriesOnly, mappings, base);
                            completedCout++;
                            feedback.setPercetageComplete((int) (completedCout * 100 / todoCount));
                        }
                    }
                }
            }
            feedback.setPercetageComplete(100);
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
    }

    private void processBuildDirectory(File buildDir, NonDeadDirectoryFilter directoriesOnly, Map<Long, Long> mappings, File base) throws IOException
    {
        for (File recipeDir : buildDir.listFiles(directoriesOnly))
        {
            Long recipeResultId = Long.valueOf(recipeDir.getName());
            if (mappings.containsKey(recipeResultId))
            {
                Long projectId = mappings.get(recipeResultId);

                // move the build directory into the specified project.
                File mappedProjectDir = new File(base, Long.toString(projectId));
                if (!mappedProjectDir.isDirectory() && !mappedProjectDir.mkdirs())
                {
                    throw new IOException("Failed to create new project directory: " + mappedProjectDir.getCanonicalPath());
                }

                File newBuildDir = new File(mappedProjectDir, buildDir.getName());
                if (!buildDir.renameTo(newBuildDir))
                {
                    throw new IOException("Failed to move " + buildDir.getCanonicalPath() + " to " + newBuildDir.getCanonicalPath());
                }
            }
        }
    }

    public void setUserPaths(MasterUserPaths paths)
    {
        this.paths = paths;
    }

    private Map<Long, Long> readMappings(File file) throws IOException
    {
        Map<Long, Long> mappings = new HashMap<Long, Long>();

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("#"))
                {
                    continue;
                }
                int index = line.indexOf("->");
                Long recipeResultId = Long.valueOf(line.substring(0, index));
                Long projectId = Long.valueOf(line.substring(index + 2));
                mappings.put(recipeResultId, projectId);
            }
        }
        finally
        {
            IOUtils.close(reader);
        }

        return mappings;
    }

    public void setFeedback(Feedback feedback)
    {
        this.feedback = feedback;
    }

    public void setSystemConfiguration(SystemConfiguration systemConfiguration)
    {
        this.systemConfiguration = systemConfiguration;
    }

    private class NonDeadDirectoryFilter implements FileFilter
    {
        public boolean accept(File file)
        {
            return file.isDirectory() && !file.getName().endsWith(".dead");
        }
    }
}

