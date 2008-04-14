package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.MasterUserPaths;
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

    public String getName()
    {
        return "artifacts";
    }

    public String getDescription()
    {
        return "The artifacts restoration consists of moving around the directories located within the " +
                "PULSE_DATA/projects directory to match the restructured project hierarchy.  This step is " +
                "only necessary if the projects directory has been manually transfered from the 1.2.x " +
                "PULSE_DATA directory into the 2.0 PULSE_DATA directory.";
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

            // a) get a quick overall directory count.
            DirectoryFilter directoriesOnly = new DirectoryFilter();

            long todoCount = 0;
            for (File projectDir : base.listFiles(directoriesOnly))
            {
                File[] listing = projectDir.listFiles(directoriesOnly);
                if (listing != null)
                {
                    todoCount = todoCount + listing.length;
                }
            }

            long completedCout = 0;
            for (File projectDir : base.listFiles(new DirectoryFilter()))
            {
                for (File buildDir : projectDir.listFiles(new DirectoryFilter()))
                {
                    for (File recipeDir : buildDir.listFiles(new DirectoryFilter()))
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

                        completedCout++;

                        feedback.setPercetageComplete((int)(completedCout * 100 / todoCount));
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
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

    private class DirectoryFilter implements FileFilter
    {
        public boolean accept(File file)
        {
            return file.isDirectory();
        }
    }
}

