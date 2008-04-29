package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.MasterUserPaths;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.restore.feedback.Feedback;
import com.zutubi.pulse.restore.feedback.FeedbackProvider;
import com.zutubi.util.IOUtils;
import com.zutubi.util.Pair;
import com.zutubi.util.StringUtils;

import java.io.*;
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
            if (mappingsFile.isFile())
            {
                Map<Pair<String, String>, String> mappings = readMappings(mappingsFile);

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
                        String fromProject = projectDir.getName();
                        for (File buildDir : projectDir.listFiles(directoriesOnly))
                        {
                            if (buildDir.getName().equals("builds"))
                            {
                                for (File nestedBuildDir : buildDir.listFiles(directoriesOnly))
                                {
                                    processBuildDirectory(fromProject, nestedBuildDir, mappings, base);
                                    completedCout++;
                                    feedback.setPercetageComplete((int) (completedCout * 100 / todoCount));
                                }
                            }
                            else
                            {
                                processBuildDirectory(fromProject, buildDir, mappings, base);
                                completedCout++;
                                feedback.setPercetageComplete((int) (completedCout * 100 / todoCount));
                            }
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

    private void processBuildDirectory(String fromProject, File buildDir, Map<Pair<String, String>, String> mappings, File base) throws IOException
    {
        // move the build directory into the specified project.
        String buildNumber = buildDir.getName();
        String toProject = mappings.get(new Pair<String, String>(fromProject, buildNumber));

        if (toProject != null)
        {
            File mappedProjectDir = new File(base, toProject);
            if (!mappedProjectDir.isDirectory() && !mappedProjectDir.mkdirs())
            {
                throw new IOException("Failed to create new project directory: " + mappedProjectDir.getCanonicalPath());
            }

            File newBuildDir = new File(mappedProjectDir, buildNumber);
            if (!buildDir.renameTo(newBuildDir))
            {
                throw new IOException("Failed to move " + buildDir.getCanonicalPath() + " to " + newBuildDir.getCanonicalPath());
            }
        }
    }

    public void setUserPaths(MasterUserPaths paths)
    {
        this.paths = paths;
    }

    private Map<Pair<String, String>, String> readMappings(File file) throws IOException
    {
        Map<Pair<String, String>, String> mappings = new HashMap<Pair<String, String>, String>();

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

                String[] pieces = StringUtils.split(line, ',');
                if(pieces.length != 3)
                {
                    continue;
                }

                mappings.put(new Pair<String, String>(pieces[0], pieces[1]), pieces[2]);
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

