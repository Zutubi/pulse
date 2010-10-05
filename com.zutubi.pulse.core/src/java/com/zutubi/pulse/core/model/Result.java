package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.TimeStamps;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base for all results (e.g. command results, build results).  Stores
 * information about when the result was generated, its status and so on.
 */
public abstract class Result extends Entity
{
    protected static final int UNDEFINED = -1;

    // NOTE: if you add a field here, check the update() method in
    // CommandResult!
    protected ResultState state = ResultState.PENDING;
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
        return ResultState.PENDING == getState();
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

    public void skip()
    {
        state = ResultState.SKIPPED;
    }

    public boolean skipped()
    {
        return state == ResultState.SKIPPED;
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
        return state.isCompleted();
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

    public void failure()
    {
        if (state != ResultState.ERROR && state != ResultState.TERMINATING)
        {
            state = ResultState.FAILURE;
        }
    }

    public void error()
    {
        if (state != ResultState.TERMINATING)
        {
            state = ResultState.ERROR;
        }
    }

    public void addFeature(Feature.Level level, String message)
    {
        addFeature(new PersistentFeature(level, message));
    }

    public void addFeature(PersistentFeature feature)
    {
        if (!features.contains(feature))
        {
            features.add(feature);
        }
    }

    public void failure(String message)
    {
        failure();
        addFeature(Feature.Level.ERROR, message);
    }

    public void error(String message)
    {
        error();
        addFeature(Feature.Level.ERROR, message);
    }

    public void warning(String message)
    {
        addFeature(Feature.Level.WARNING, message);
    }

    public void terminate(String message)
    {
        state = ResultState.TERMINATING;
        addFeature(Feature.Level.ERROR, message);
    }

    public void error(BuildException e)
    {
        error(e.getMessage());
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

    /**
     * Returns the absolute output path, based on the given data root.
     *
     * @param dataRoot the PULSE_DATA directory
     * @return the absolute path of the output directory for this result.
     */
    public File getAbsoluteOutputDir(File dataRoot)
    {
        return outputDir == null ? null : new File(dataRoot, outputDir.getPath());
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
        return outputDir == null ? null : outputDir.getPath();
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
