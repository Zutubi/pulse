package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.TimeStamps;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public abstract class Result extends Entity
{
    private static final String EXCEPTION_FILE = "exception";

    protected ResultState state = ResultState.INITIAL;
    protected TimeStamps stamps = new TimeStamps();
    private File outputDir;
    protected List<Feature> features = new LinkedList<Feature>();

    public boolean pending()
    {
        return ResultState.INITIAL == getState();
    }

    public boolean inProgress()
    {
        return ResultState.IN_PROGRESS == getState();
    }

    public boolean terminating()
    {
        return ResultState.TERMINATING == getState();
    }

    public boolean succeeded()
    {
        return ResultState.SUCCESS == getState();
    }

    public boolean failed()
    {
        return ResultState.FAILURE == getState();
    }

    public boolean errored()
    {
        return ResultState.ERROR == getState();
    }

    public boolean commenced()
    {
        return inProgress() || terminating() || completed();
    }

    public boolean completed()
    {
        return succeeded() || errored() || failed();
    }

    public void queue()
    {
        stamps.setQueueTime(System.currentTimeMillis());
    }

    public void commence()
    {
        state = ResultState.IN_PROGRESS;
        stamps = new TimeStamps();
        stamps.setStartTime(System.currentTimeMillis());
    }

    public void commence(long startTime)
    {
        stamps.setStartTime(startTime);
        // Special case: marked as terminating before we commenced.
        if (state != ResultState.TERMINATING)
        {
            state = ResultState.IN_PROGRESS;
        }
    }

    public void complete()
    {
        if (state == ResultState.IN_PROGRESS)
        {
            // Phew, nothing went wrong.
            state = ResultState.SUCCESS;
        }
        else if (state == ResultState.TERMINATING)
        {
            state = ResultState.ERROR;
        }

        if (!stamps.started())
        {
            // CIB-234: if an error occurs before starting the stamps, make
            // sure we still get a start time (otherwise there is no
            // information about when this result happened!)
            stamps.setStartTime(System.currentTimeMillis());
        }
        stamps.end();
    }

    public void success()
    {
        state = ResultState.SUCCESS;
    }

    public void failure()
    {
        state = ResultState.FAILURE;
    }

    public void addFeature(Feature.Level level, String message)
    {
        Feature feature = new Feature(level, message);

        // Eliminate duplicates
        for (Feature f : features)
        {
            if (feature.equals(f))
            {
                return;
            }
        }

        features.add(feature);
    }

    public void failure(String message)
    {
        failure();
        addFeature(Feature.Level.ERROR, message);
    }

    public void error(String message)
    {
        state = ResultState.ERROR;
        addFeature(Feature.Level.ERROR, message);
    }

    public void terminate(boolean timeout)
    {
        state = ResultState.TERMINATING;
        if (timeout)
        {
            addFeature(Feature.Level.ERROR, "Timed out");
        }
        else
        {
            addFeature(Feature.Level.ERROR, "Forcefully terminated");
        }
    }

    public void error(BuildException e)
    {
        error(e.getMessage());

        if (outputDir != null)
        {

            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream(new File(outputDir, EXCEPTION_FILE));
                e.printStackTrace(new PrintStream(fos, true));
            }
            catch (FileNotFoundException ignored)
            {
                // no need to handle this, we did our level best
            }
            finally
            {
                IOUtils.close(fos);
            }
        }
    }

    public ResultState getState()
    {
        return state;
    }

    public void setState(ResultState state)
    {
        this.state = state;
    }

    public String getStateName()
    {
        return state.name();
    }

    private void setStateName(String name)
    {
        state = ResultState.valueOf(name);
    }

    public TimeStamps getStamps()
    {
        return stamps;
    }

    private void setStamps(TimeStamps stamps)
    {
        this.stamps = stamps;
    }

    public String getExceptionTrace()
    {
        File exceptionFile = new File(outputDir, EXCEPTION_FILE);
        String result = null;

        try
        {
            result = IOUtils.fileToString(exceptionFile);
        }
        catch (IOException e)
        {
            // Empty
        }

        return result;
    }

    /**
     * Returns the absolute output path, based on the given data root.
     *
     * @param dataRoot the PULSE_DATA directory
     * @return the absolute path of the output directory for this result.
     */
    public File getAbsoluteOutputDir(File dataRoot)
    {
        return new File(dataRoot, outputDir.getPath());
    }

    /**
     * Sets the output path based on the given data root and absolute output
     * directory.
     *
     * @param dataRoot the PULSE_DATA directory
     * @param outputDir absolute path of the output directory to set
     */
    public void setAbsoluteOutputDir(File dataRoot, File outputDir)
    {
        String dataPath = dataRoot.getAbsolutePath();
        String outputPath = outputDir.getAbsolutePath();
        if(outputPath.startsWith(dataPath))
        {
            outputPath = outputPath.substring(dataPath.length());
            if(outputPath.startsWith(File.separator) || outputPath.startsWith("/"))
            {
                outputPath = outputPath.substring(1);
            }
        }

        this.outputDir = new File(outputPath);
    }

    public String getOutputDir()
    {
        if (outputDir == null)
        {
            return null;
        }
        else
        {
            return outputDir.getPath();
        }
    }

    public void setOutputDir(String dir)
    {
        if (dir == null)
        {
            outputDir = null;
        }
        else
        {
            outputDir = new File(dir);
        }
    }

    public List<Feature> getFeatures()
    {
        return features;
    }

    public List<Feature> getFeatures(Feature.Level level)
    {
        List<Feature> result = new LinkedList<Feature>();
        for (Feature f : features)
        {
            if (f.getLevel() == level)
            {
                result.add(f);
            }
        }

        return result;
    }

    private void setFeatures(List<Feature> features)
    {
        this.features = features;
    }

    public List<String> collectErrors()
    {
        List<String> errors = new LinkedList<String>();

        for (Feature f : features)
        {
            if (f.getLevel() == Feature.Level.ERROR)
            {
                errors.add(f.getSummary());
            }
        }

        return errors;
    }

    public boolean hasDirectMessages(Feature.Level level)
    {
        for (Feature f : features)
        {
            if (f.getLevel() == level)
            {
                return true;
            }
        }

        return false;
    }

    public Feature.Level getLevel(String name)
    {
        return Feature.Level.valueOf(name);
    }

    public abstract void accumulateTestSummary(TestResultSummary summary);

    public boolean hasTests()
    {
        return getTestSummary().getTotal() > 0;
    }

    public boolean hasBrokenTests()
    {
        return getTestSummary().getBroken() > 0;
    }

    public TestResultSummary getTestSummary()
    {
        TestResultSummary summary = new TestResultSummary();
        accumulateTestSummary(summary);
        return summary;
    }
}
