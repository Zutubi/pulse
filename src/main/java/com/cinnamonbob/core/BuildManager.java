package com.cinnamonbob.core;

import com.cinnamonbob.bootstrap.BootstrapUtils;
import com.cinnamonbob.util.FileSystemUtils;
import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 *
 */
public class BuildManager
{
    private static final Logger LOG = Logger.getLogger(BuildManager.class.getName());

    /**
     *
     */
    private static final String BUILD_ROOT = "builds";

    /**
     *
     */
    private static final String WORK_ROOT = "work";

    private static final String RESULT_FILE_NAME = "result.xml";

    /**
     * Used for (de)serialisation.
     */
    private XStream xstream;

    private String buildDirectory;
    private String workDirectory;

    private int nextBuild = 0;

    private static BuildManager INSTANCE;
    private static Object lock = new Object();

    private BuildManager()
    {
        init();
    }

    /**
     * Initialise this manager.
     */
    private void init()
    {
        File projectRoot = getProjectRoot();
        buildDirectory = new File(projectRoot, BUILD_ROOT).getAbsolutePath();
        workDirectory = new File(projectRoot, WORK_ROOT).getAbsolutePath();

        nextBuild = determineNextAvailableBuildId();
        if (nextBuild == 0)
        {
            getBuildRoot().mkdirs();
        }
        xstream = new XStream();
    }

    /**
         * Get the singleton instance of this BuildManager.
         *
         * @return
         */
    public static final BuildManager getInstance()
    {
        if (INSTANCE == null)
        {
            synchronized (lock)
            {
                if (INSTANCE == null)
                {
                    INSTANCE = new BuildManager();
                }
            }
        }
        return INSTANCE;
    }

    public File getBuildRoot()
    {
        return new File(buildDirectory);
    }

    public File getWorkRoot()
    {
        return new File(workDirectory);
    }

    public File getProjectRoot()
    {
        return new File(BootstrapUtils.getManager().getApplicationPaths().getApplicationRoot(), "work");
    }

    private int determineNextAvailableBuildId()
    {
        // Determine next build id
        File buildsDir = getBuildRoot();

        if (buildsDir.isDirectory())
        {
            String files[] = buildsDir.list();
            int max = -1;

            for (int i = 0; i < files.length; i++)
            {
                try
                {
                    int buildNumber = Integer.parseInt(files[i]);

                    if (buildNumber > max)
                    {
                        max = buildNumber;
                    }
                } catch (NumberFormatException e)
                {
                    // Oh well, not a build dir
                }
            }

            return max + 1;
        } else
        {
            return 0;
        }
    }

    public BuildResult executeBuild(Project project)
    {
        // Allocate the result with a unique id.
        BuildResult result = new BuildResult(project.getName(), nextBuild, project.getCategoryRegistry());
        long startTime = System.currentTimeMillis();
        File buildDir = null;

        try
        {
            cleanWorkDir();
            buildDir = createBuildDir(getBuildRoot(), result);
        } catch (InternalBuildFailureException e)
        {
            // Not even able to create the build directory: bad news.
            result.setInternalFailure(e);
            logInternalBuildFailure(result);
            return result;
        }

        // May record internal failures too.
        executeCommands(project, result, buildDir);
        result.stamp(new TimeStamps(startTime, System.currentTimeMillis()));

        try
        {
            saveBuildResult(buildDir, result);
        } catch (InternalBuildFailureException e)
        {
            // We basically can't save anything about this, so bail out.
            // Don't clobber earlier failure...
            if (result.getInternalFailure() == null)
            {
                result.setInternalFailure(e);
                logInternalBuildFailure(result);
            }
        }

        // Don't increment nextBuild until we have finished the build, this
        // way the build won't be picked up by getHistory until complete.
        synchronized (this)
        {
            nextBuild++;
        }

        return result;
    }

    private File createBuildDir(File outputDir, BuildResult buildResult) throws InternalBuildFailureException
    {
        File buildDir = getBuildDir(outputDir, buildResult.getId());

        if(!buildDir.mkdir())
        {
            throw new InternalBuildFailureException("Could not create build directory '" + buildDir.getAbsolutePath() + "'");
        }

        return buildDir;
    }

    private File getBuildDir(File outputDir, int buildId)
	{
        String dirName = String.format("%08d", new Integer(buildId));
        return new File(outputDir, dirName);
	}


    private void logInternalBuildFailure(BuildResult result)
    {
        InternalBuildFailureException e = result.getInternalFailure();

        LOG.severe("Project '" + result.getProjectName() + "' build " + Integer.toString(result.getId()) + ": Internal build failure:");
        LOG.severe(e.getMessage());

        if (e.getCause() != null)
        {
            LOG.severe("Cause: " + e.getCause().getMessage());
        }
    }

    private void cleanWorkDir() throws InternalBuildFailureException
    {
        File workDir = getWorkRoot();
        if(workDir.exists())
        {
            if(!FileSystemUtils.removeDirectory(workDir))
            {
                throw new InternalBuildFailureException("Could not clean work directory '" + workDir.getAbsolutePath() + '"');
            }
        }

        if(!workDir.mkdir())
        {
            throw new InternalBuildFailureException("Could not create work directory '" + workDir.getAbsolutePath() + "'");
        }
    }

    private void executeCommands(Project project, BuildResult result, File buildDir)
    {
        try
        {
            int i = 0;
            boolean failed = false;

            for(CommandCommon command: project.getRecipe())
            {
                if(!failed || command.getForce())
                {
                    File                commandOutputDir = createCommandOutputDir(buildDir, command, i);
                    CommandResultCommon commandResult    = command.execute(commandOutputDir);

                    result.addCommandResult(commandResult);
                    saveCommandResult(commandOutputDir, commandResult);
                    i++;

                    if(!commandResult.getResult().succeeded())
                    {
                        failed = true;
                    }
                }
            }
        }
        catch(InternalBuildFailureException e)
        {
            result.setInternalFailure(e);
            logInternalBuildFailure(result);
        }
    }

    private void saveBuildResult(File buildDir, BuildResult result) throws InternalBuildFailureException
    {
        File resultFile = new File(buildDir, RESULT_FILE_NAME);

        try
        {
            xstream.toXML(result, new FileWriter(resultFile));
        }
        catch(IOException e)
        {
            throw new InternalBuildFailureException("Could not save build result to file '" + resultFile.getAbsolutePath() + "'", e);
        }
    }


    private void saveCommandResult(File commandOutputDir, CommandResultCommon commandResult) throws InternalBuildFailureException
    {
        File resultFile = new File(commandOutputDir, RESULT_FILE_NAME);

        try
        {
            xstream.toXML(commandResult, new FileWriter(resultFile));
        }
        catch(IOException e)
        {
            throw new InternalBuildFailureException("Could not save command result to file '" + resultFile.getAbsolutePath() + "'", e);
        }
    }

    private File createCommandOutputDir(File buildDir, CommandCommon command, int index) throws InternalBuildFailureException
    {
        String dirName        = String.format("%08d-%s", index, command.getName());
        File commandOutputDir = new File(buildDir, dirName);

        if(!commandOutputDir.mkdir())
        {
            throw new InternalBuildFailureException("Could not create command output directory '" + commandOutputDir.getAbsolutePath() + "'");
        }

        return commandOutputDir;
    }

    private void loadCommandResults(File buildDir, BuildResult result)
    {
        if(buildDir.isDirectory())
        {
            String files[] = buildDir.list();
            Arrays.sort(files);

            for(String dirName: files)
            {
                File dir = new File(buildDir, dirName);

                if(dir.isDirectory())
                {
                    File resultFile = new File(dir, "result.xml");

                    try
                    {
                        CommandResultCommon commandResult = (CommandResultCommon)xstream.fromXML(new FileReader(resultFile));
                        result.addCommandResult(commandResult);
                    }
                    catch(FileNotFoundException e)
                    {
                        LOG.warning("I/O error loading command result from file '" + resultFile.getAbsolutePath() + "': " + e.getMessage());
                    }
                }
            }
        }
    }

	/**
	 * Retrieves a history of recent builds of this project.  The history may
	 * be shorter than requested (even empty) if there have not been enough
	 * previous builds.
	 *
	 * @param maxBuilds
	 *        the maximum number of results to return
	 * @return a list of recent build results, most recent first
	 */
	public List<BuildResult> getHistory(Project project, int maxBuilds)
	{
		int latestBuild;
		List<BuildResult> history = new LinkedList<BuildResult>();

		synchronized(this)
		{
			latestBuild = nextBuild - 1;
		}

		for(int i = latestBuild; i >= 0 && history.size() < maxBuilds; i--)
		{
			BuildResult result = loadBuild(project, i);
			if(result != null)
			{
				history.add(result);
			}
		}

		return history;
	}

    private BuildResult loadBuild(Project project, int buildId)
    {
        File        buildDir   = getBuildDir(getBuildRoot(), buildId);
        File        resultFile = new File(buildDir, RESULT_FILE_NAME);
        BuildResult result     = null;

        try
        {
            result = (BuildResult)xstream.fromXML(new FileReader(resultFile));
            result.load(project.getName(), buildId, buildDir);
            loadCommandResults(buildDir, result);
        }
        catch(IOException e)
        {
            LOG.warning("I/O error loading build result from file '" + resultFile.getAbsolutePath() + "'");
        }

        return result;
    }

    public BuildResult getBuildResult(Project project, int id)
    {
        return loadBuild(project, id);
    }
}
