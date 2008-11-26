package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.util.TimeStamps;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public abstract class Result extends Entity
{
    private static final String EXCEPTION_FILE = "exception";
    protected static final int UNDEFINED = -1;

    // NOTE: if you add a field here, check the update() method in
    // CommandResult!
    protected ResultState state = ResultState.INITIAL;
    protected TimeStamps stamps = new TimeStamps();
    private File outputDir;
    protected List<PersistentFeature> features = new LinkedList<PersistentFeature>();

    /**
     * A count of the number of warning features associated with this result.
     */
    protected int warningFeatureCount = UNDEFINED;

    /**
     * A count of the number of error features associated with this result.  
     */
    protected int errorFeatureCount = UNDEFINED;

    /**
     * A result that is pending is waiting to for processing to commence. Pending is the
     * initial state for a result.
     *
     * @return true if this result is pending.
     */
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

    /**
     * A result is marked as commenced if it is 'inProcess|terminating|completed'. 
     *
     * @return true if the process has commenced, false otherwise.
     */
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
        commence(System.currentTimeMillis());
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

    public long getStartTime()
    {
        return stamps.getStartTime();
    }

    public void complete()
    {
        complete(System.currentTimeMillis());
    }

    public void complete(long endTime)
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
            stamps.setStartTime(endTime);
        }
        stamps.setEndTime(endTime);
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
        PersistentFeature feature = new PersistentFeature(level, message);
        addFeature(feature);
    }

    public void addFeature(PersistentFeature feature)
    {
        // Eliminate duplicates
        for (PersistentFeature f : features)
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

    public void warning(String message)
    {
        addFeature(Feature.Level.WARNING, message);
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
        String result = null;
        if (outputDir != null)
        {
            try
            {
                File exceptionFile = new File(outputDir, EXCEPTION_FILE);
                result = IOUtils.fileToString(exceptionFile);
            }
            catch (IOException e)
            {
                // Empty
            }
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
        if (outputDir == null)
        {
            return null;
        }
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

    public List<PersistentFeature> getFeatures()
    {
        return features;
    }

    public List<PersistentFeature> getFeatures(Feature.Level level)
    {
        List<PersistentFeature> result = new LinkedList<PersistentFeature>();
        for (PersistentFeature f : features)
        {
            if (f.getLevel() == level)
            {
                result.add(f);
            }
        }

        return result;
    }

    private void setFeatures(List<PersistentFeature> features)
    {
        this.features = features;
    }

    public List<String> collectErrors()
    {
        List<String> errors = new LinkedList<String>();

        for (PersistentFeature f : features)
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
        for (PersistentFeature f : features)
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

    public int getFeatureCount(Feature.Level level)
    {
        switch(level)
        {
            case ERROR:
                return errorFeatureCount;
            case WARNING:
                return warningFeatureCount;
            default:
                throw new IllegalArgumentException("Feature count not cached for level '" + level + "'");
        }
    }
    
    /**
     * Getter for the warning feature count property.
     *
     * @return the warning feature count.
     */
    public int getWarningFeatureCount()
    {
        return warningFeatureCount;
    }

    /**
     * Setter for the warning feature count property.
     *
     * @param count     warning feature count.
     */
    public void setWarningFeatureCount(int count)
    {
        this.warningFeatureCount = count;
    }

    /**
     * Getter for the error feature count property.
     *
     * @return the error feature count.
     */
    public int getErrorFeatureCount()
    {
        return errorFeatureCount;
    }

    /**
     * Setter for the error feature count property.
     *
     * @param count     error feature count.
     */
    public void setErrorFeatureCount(int count)
    {
        this.errorFeatureCount = count;
    }

    /**
     * Calculate the feature counts for this result instance.
     */
    public void calculateFeatureCounts()
    {
        warningFeatureCount = 0;
        errorFeatureCount = 0;

        for (PersistentFeature f : features)
        {
            if (f.getLevel() == Feature.Level.ERROR)
            {
                errorFeatureCount++;
            }
            if (f.getLevel() == Feature.Level.WARNING)
            {
                warningFeatureCount++;
            }
        }
    }
}
